-- PHẦN 4: TRIGGER
-- ============================================================

-- 4.1 Tự thiết lập vé giữ chỗ khi INSERT
CREATE OR REPLACE TRIGGER trg_set_ve_giu_cho
BEFORE INSERT ON VE
FOR EACH ROW
BEGIN
    IF :NEW.TrangThai IS NULL THEN
        :NEW.TrangThai := 'Giữ chỗ';
    END IF;

    IF :NEW.TrangThai = 'Giữ chỗ' AND :NEW.ThoiGianGiuDen IS NULL THEN
        :NEW.ThoiGianGiuDen := SYSTIMESTAMP + INTERVAL '15' MINUTE;
    END IF;
END;
/

-- 4.2 Ghế phải thuộc đúng xe của chuyến
CREATE OR REPLACE TRIGGER trg_check_ghe_thuoc_xe_chuyen
BEFORE INSERT OR UPDATE ON VE
FOR EACH ROW
DECLARE
    v_MaXe_cx XE.MaXe%TYPE;
    v_MaXe_gh XE.MaXe%TYPE;
BEGIN
    BEGIN
        SELECT MaXe INTO v_MaXe_cx
        FROM CHUYENXE
        WHERE MaChuyen = :NEW.MaChuyen;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20001,
            'Chuyến ' || :NEW.MaChuyen || ' không tồn tại.');
    END;

    BEGIN
        SELECT MaXe INTO v_MaXe_gh
        FROM GHE
        WHERE MaGhe = :NEW.MaGhe;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20002,
            'Ghế ' || :NEW.MaGhe || ' không tồn tại.');
    END;

    IF v_MaXe_cx != v_MaXe_gh THEN
        RAISE_APPLICATION_ERROR(-20003,
            'Ghế ' || :NEW.MaGhe ||
            ' không thuộc xe của chuyến ' || :NEW.MaChuyen || '.');
    END IF;
END;
/

-- 4.3 Kiểm tra điểm đón/trả
CREATE OR REPLACE TRIGGER trg_check_diemdon_diemtra
BEFORE INSERT OR UPDATE ON VE
FOR EACH ROW
DECLARE
    v_CDon  DIEMDONTRA.MaChuyen%TYPE;
    v_CTra  DIEMDONTRA.MaChuyen%TYPE;
    v_LDon  DIEMDONTRA.Loai%TYPE;
    v_LTra  DIEMDONTRA.Loai%TYPE;
    v_TDon  DIEMDONTRA.ThuTu%TYPE;
    v_TTra  DIEMDONTRA.ThuTu%TYPE;
BEGIN
    IF :NEW.MaDiemDon = :NEW.MaDiemTra THEN
        RAISE_APPLICATION_ERROR(-20004,
            'Điểm đón và điểm trả không được trùng nhau.');
    END IF;

    BEGIN
        SELECT MaChuyen, Loai, ThuTu
        INTO v_CDon, v_LDon, v_TDon
        FROM DIEMDONTRA
        WHERE MaDiem = :NEW.MaDiemDon;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20005,
            'Điểm đón ' || :NEW.MaDiemDon || ' không tồn tại.');
    END;

    BEGIN
        SELECT MaChuyen, Loai, ThuTu
        INTO v_CTra, v_LTra, v_TTra
        FROM DIEMDONTRA
        WHERE MaDiem = :NEW.MaDiemTra;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20006,
            'Điểm trả ' || :NEW.MaDiemTra || ' không tồn tại.');
    END;

    IF v_CDon != :NEW.MaChuyen OR v_CTra != :NEW.MaChuyen THEN
        RAISE_APPLICATION_ERROR(-20007,
            'Điểm đón/trả không thuộc chuyến ' || :NEW.MaChuyen || '.');
    END IF;

    IF v_LDon != 'Đón' OR v_LTra != 'Trả' THEN
        RAISE_APPLICATION_ERROR(-20008,
            'Loại điểm không hợp lệ (đón=' || v_LDon ||
            ', trả=' || v_LTra || ').');
    END IF;

    IF v_TDon >= v_TTra THEN
        RAISE_APPLICATION_ERROR(-20009,
            'Điểm đón (ThuTu=' || v_TDon ||
            ') phải trước điểm trả (ThuTu=' || v_TTra || ').');
    END IF;
END;
/

-- 4.4 Hủy vé khi đơn đặt bị hủy
CREATE OR REPLACE TRIGGER trg_huy_ve_khi_datve_huy
AFTER UPDATE ON DATVE
FOR EACH ROW
BEGIN
    IF :OLD.TrangThai != 'Đã hủy'
       AND :NEW.TrangThai = 'Đã hủy' THEN
        UPDATE VE
        SET TrangThai = 'Đã hủy'
        WHERE MaDatVe   = :NEW.MaDatVe
          AND TrangThai IN ('Giữ chỗ', 'Đã đặt');
    END IF;
END;
/

-- 4.5 Cập nhật đơn sau thanh toán thành công
CREATE OR REPLACE TRIGGER trg_cap_nhat_don_sau_thanhtoan
AFTER INSERT ON THANHTOAN
FOR EACH ROW
DECLARE
    v_MaDatVe  DATVE.MaDatVe%TYPE;
    v_TongTien HOADON.TongTien%TYPE;
BEGIN
    IF :NEW.TrangThai != 'Thành công'
       OR :NEW.LoaiGiaoDich != 'ThanhToan' THEN
        RETURN;
    END IF;

    BEGIN
        SELECT TongTien, MaDatVe
        INTO v_TongTien, v_MaDatVe
        FROM HOADON
        WHERE MaHoaDon = :NEW.MaHoaDon;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20010,
            'Hóa đơn ' || :NEW.MaHoaDon || ' không tồn tại.');
    END;

    IF :NEW.SoTien != v_TongTien THEN
        RAISE_APPLICATION_ERROR(-20011,
            'Số tiền (' || :NEW.SoTien ||
            ') không khớp hóa đơn (' || v_TongTien || ').');
    END IF;

    UPDATE HOADON
    SET TrangThai = 'Đã thanh toán'
    WHERE MaHoaDon = :NEW.MaHoaDon;

    UPDATE DATVE
    SET TrangThai = 'Đã thanh toán'
    WHERE MaDatVe = v_MaDatVe;

    UPDATE VE
    SET TrangThai = 'Đã đặt'
    WHERE MaDatVe = v_MaDatVe
      AND TrangThai = 'Giữ chỗ';
END;
/

-- 4.6 Tính TongTien = GiaGoc - TienGiam
CREATE OR REPLACE TRIGGER trg_tinh_tongtien_hoadon
BEFORE INSERT OR UPDATE ON HOADON
FOR EACH ROW
DECLARE
    v_TongTien NUMBER;
BEGIN
    v_TongTien := :NEW.GiaGoc - :NEW.TienGiam;

    IF v_TongTien < 0 THEN
        RAISE_APPLICATION_ERROR(-20012,
            'Tiền giảm (' || :NEW.TienGiam ||
            ') không được lớn hơn giá gốc (' || :NEW.GiaGoc || ').');
    END IF;

    :NEW.TongTien := v_TongTien;
END;
/

-- 4.7 Chỉ đánh giá khi chuyến hoàn thành và khách có vé hợp lệ
CREATE OR REPLACE TRIGGER trg_check_danhgia_da_di_chuyen
BEFORE INSERT ON DANHGIA
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM VE v
    JOIN DATVE    dv ON v.MaDatVe  = dv.MaDatVe
    JOIN CHUYENXE cx ON v.MaChuyen = cx.MaChuyen
    WHERE v.MaChuyen   = :NEW.MaChuyen
      AND dv.MaKH      = :NEW.MaKH
      AND v.TrangThai  IN ('Đã đặt','Đã dùng')
      AND cx.TrangThai = 'Hoàn thành';

    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20013,
            'Chuyến chưa hoàn thành hoặc bạn chưa đi chuyến này.');
    END IF;
END;
/

-- 4.8 Khóa tài khoản khi nhân viên nghỉ việc
CREATE OR REPLACE TRIGGER trg_khoa_tk_khi_nv_nghi_viec
AFTER UPDATE ON NHANVIEN
FOR EACH ROW
BEGIN
    IF :OLD.TrangThai != 'Nghỉ việc'
       AND :NEW.TrangThai = 'Nghỉ việc' THEN
        UPDATE TAIKHOAN
        SET TrangThaiTK = 'Bị khóa'
        WHERE MaTK = :NEW.MaTK;
    END IF;
END;
/

-- ============================================================
