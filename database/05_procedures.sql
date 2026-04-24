-- PHẦN 5: STORED PROCEDURE
-- ============================================================

-- 5.1 Tính tiền giảm khuyến mãi
CREATE OR REPLACE PROCEDURE sp_tinh_tien_giam (
    p_MaKhuyenMai IN  VARCHAR2,
    p_GiaGoc      IN  NUMBER,
    p_TienGiam    OUT NUMBER
) AS
    v_Trang    VARCHAR2(20);
    v_PTram    NUMBER;
    v_STien    NUMBER;
    v_BatDau   TIMESTAMP;
    v_KetThuc  TIMESTAMP;
BEGIN
    SELECT TrangThai, PhanTramGiam, SoTienGiam,
           NgayBatDau, NgayKetThuc
    INTO v_Trang, v_PTram, v_STien, v_BatDau, v_KetThuc
    FROM KHUYENMAI
    WHERE MaKhuyenMai = p_MaKhuyenMai;

    IF v_Trang != 'Đang áp dụng'
       OR SYSTIMESTAMP NOT BETWEEN v_BatDau AND v_KetThuc THEN
        RAISE_APPLICATION_ERROR(-20050,
            'Khuyến mãi không còn hiệu lực.');
    END IF;

    IF v_PTram > 0 THEN
        p_TienGiam := ROUND(p_GiaGoc * v_PTram / 100);
    ELSE
        p_TienGiam := v_STien;
    END IF;

    IF p_TienGiam > p_GiaGoc THEN
        p_TienGiam := p_GiaGoc;
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20051, 'Khuyến mãi không tồn tại.');
END sp_tinh_tien_giam;
/

-- 5.2 Đặt vé
CREATE OR REPLACE PROCEDURE sp_dat_ve (
    p_MaKH        IN  VARCHAR2,
    p_MaChuyen    IN  VARCHAR2,
    p_MaGhe1      IN  VARCHAR2,
    p_MaGhe2      IN  VARCHAR2  DEFAULT NULL,
    p_MaGhe3      IN  VARCHAR2  DEFAULT NULL,
    p_MaGhe4      IN  VARCHAR2  DEFAULT NULL,
    p_MaDiemDon   IN  VARCHAR2,
    p_MaDiemTra   IN  VARCHAR2,
    p_MaLoaiVe    IN  VARCHAR2,
    p_MaKhuyenMai IN  VARCHAR2  DEFAULT NULL,
    p_MaDatVe     OUT VARCHAR2,
    p_MaHoaDon    OUT VARCHAR2,
    p_TongTien    OUT NUMBER
) AS
    TYPE t_ghe IS TABLE OF VARCHAR2(20) INDEX BY PLS_INTEGER;
    v_DsGhe     t_ghe;
    v_Idx       PLS_INTEGER := 0;
    v_GiaVe     NUMBER;
    v_GiaGoc    NUMBER;
    v_TienGiam  NUMBER := 0;
    v_MaXe      VARCHAR2(20);
    v_MaXeGhe   VARCHAR2(20);
    v_count     NUMBER;
BEGIN
    -- Gom ghế vào mảng
    IF p_MaGhe1 IS NOT NULL THEN v_Idx := v_Idx + 1; v_DsGhe(v_Idx) := p_MaGhe1; END IF;
    IF p_MaGhe2 IS NOT NULL THEN v_Idx := v_Idx + 1; v_DsGhe(v_Idx) := p_MaGhe2; END IF;
    IF p_MaGhe3 IS NOT NULL THEN v_Idx := v_Idx + 1; v_DsGhe(v_Idx) := p_MaGhe3; END IF;
    IF p_MaGhe4 IS NOT NULL THEN v_Idx := v_Idx + 1; v_DsGhe(v_Idx) := p_MaGhe4; END IF;

    IF v_Idx = 0 THEN
        RAISE_APPLICATION_ERROR(-20020, 'Phải chọn ít nhất 1 ghế.');
    END IF;

    -- Không cho chọn trùng ghế trong cùng một lần đặt
    FOR i IN 1..v_Idx LOOP
        FOR j IN i+1..v_Idx LOOP
            IF v_DsGhe(i) = v_DsGhe(j) THEN
                RAISE_APPLICATION_ERROR(-20024,
                    'Danh sách ghế bị trùng: ' || v_DsGhe(i));
            END IF;
        END LOOP;
    END LOOP;

    -- Kiểm tra khách hàng hợp lệ
    SELECT COUNT(*)
    INTO v_count
    FROM KHACHHANG
    WHERE MaKH = p_MaKH
      AND TrangThai = 'Hoạt động';

    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20025,
            'Khách hàng không tồn tại hoặc đang bị khóa.');
    END IF;

    -- Kiểm tra chuyến
    BEGIN
        SELECT GiaVe, MaXe
        INTO v_GiaVe, v_MaXe
        FROM CHUYENXE
        WHERE MaChuyen = p_MaChuyen
          AND TrangThai = 'Sắp chạy'
          AND ThoiGianKhoiHanh > SYSTIMESTAMP;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20021,
            'Chuyến không tồn tại, đã qua giờ khởi hành hoặc không ở trạng thái Sắp chạy.');
    END;

    -- Kiểm tra từng ghế
    FOR i IN 1..v_Idx LOOP
        SELECT MaXe
        INTO v_MaXeGhe
        FROM GHE
        WHERE MaGhe = v_DsGhe(i);

        IF v_MaXeGhe != v_MaXe THEN
            RAISE_APPLICATION_ERROR(-20022,
                'Ghế ' || v_DsGhe(i) || ' không thuộc xe của chuyến.');
        END IF;

        SELECT COUNT(*)
        INTO v_count
        FROM VE
        WHERE MaChuyen = p_MaChuyen
          AND MaGhe = v_DsGhe(i)
          AND TrangThai IN ('Giữ chỗ','Đã đặt','Đã dùng');

        IF v_count > 0 THEN
            RAISE_APPLICATION_ERROR(-20023,
                'Ghế ' || v_DsGhe(i) || ' đã được đặt.');
        END IF;
    END LOOP;

    -- Tạo đơn
    p_MaDatVe := 'DV' || LPAD(seq_datve.NEXTVAL, 8, '0');

    INSERT INTO DATVE (MaDatVe, MaKH, NgayDat, TrangThai)
    VALUES (p_MaDatVe, p_MaKH, SYSTIMESTAMP, 'Chờ thanh toán');

    -- Tạo vé
    FOR i IN 1..v_Idx LOOP
        INSERT INTO VE (
            MaVe, MaDatVe, MaChuyen, MaGhe,
            MaLoaiVe, MaDiemDon, MaDiemTra
        )
        VALUES (
            'VE' || LPAD(seq_ve_auto.NEXTVAL, 8, '0'),
            p_MaDatVe,
            p_MaChuyen,
            v_DsGhe(i),
            p_MaLoaiVe,
            p_MaDiemDon,
            p_MaDiemTra
        );
    END LOOP;

    -- Tính tiền
    v_GiaGoc := v_GiaVe * v_Idx;

    IF p_MaKhuyenMai IS NOT NULL THEN
        sp_tinh_tien_giam(p_MaKhuyenMai, v_GiaGoc, v_TienGiam);
    END IF;

    -- Tạo hóa đơn
    p_MaHoaDon := 'HD' || LPAD(seq_hoadon.NEXTVAL, 8, '0');
    p_TongTien := v_GiaGoc - v_TienGiam;

    INSERT INTO HOADON (
        MaHoaDon, MaDatVe, NgayLap,
        GiaGoc, TienGiam, TongTien,
        MaKhuyenMai, TrangThai
    )
    VALUES (
        p_MaHoaDon, p_MaDatVe, SYSTIMESTAMP,
        v_GiaGoc, v_TienGiam, p_TongTien,
        p_MaKhuyenMai, 'Chưa thanh toán'
    );

    COMMIT;
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20026,
            'Ghế vừa được người khác đặt hoặc mã bị trùng.');
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20027,
            'Ghế, loại vé hoặc dữ liệu liên quan không tồn tại.');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_dat_ve;
/

-- 5.3 Thanh toán
CREATE OR REPLACE PROCEDURE sp_thanh_toan (
    p_MaHoaDon    IN  VARCHAR2,
    p_PhuongThuc  IN  VARCHAR2,
    p_SoTien      IN  NUMBER,
    p_MaThanhToan OUT VARCHAR2,
    p_TrangThai   OUT VARCHAR2
) AS
    v_TongTien NUMBER;
    v_MaDatVe  VARCHAR2(30);
    v_count    NUMBER;
BEGIN
    BEGIN
        SELECT hd.TongTien, hd.MaDatVe
        INTO v_TongTien, v_MaDatVe
        FROM HOADON hd
        JOIN DATVE  dv ON hd.MaDatVe = dv.MaDatVe
        WHERE hd.MaHoaDon  = p_MaHoaDon
          AND hd.TrangThai = 'Chưa thanh toán'
          AND dv.TrangThai = 'Chờ thanh toán';
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20030,
            'Hóa đơn không tồn tại, đã thanh toán hoặc đơn không còn chờ thanh toán.');
    END;

    -- Tất cả vé của đơn phải còn ở trạng thái Giữ chỗ và chưa hết hạn
    SELECT COUNT(*)
    INTO v_count
    FROM VE
    WHERE MaDatVe = v_MaDatVe
      AND (
            TrangThai != 'Giữ chỗ'
            OR ThoiGianGiuDen < SYSTIMESTAMP
          );

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20031,
            'Đơn có vé không còn hợp lệ để thanh toán.');
    END IF;

    -- Kiểm tra số tiền
    IF p_SoTien != v_TongTien THEN
        RAISE_APPLICATION_ERROR(-20032,
            'Số tiền (' || p_SoTien ||
            ') không khớp hóa đơn (' || v_TongTien || ').');
    END IF;

    p_MaThanhToan := 'TT' || LPAD(seq_thanhtoan.NEXTVAL, 8, '0');

    INSERT INTO THANHTOAN (
        MaThanhToan, MaHoaDon, LoaiGiaoDich,
        PhuongThucThanhToan, NgayThanhToan,
        SoTien, TrangThai
    )
    VALUES (
        p_MaThanhToan, p_MaHoaDon, 'ThanhToan',
        p_PhuongThuc, SYSTIMESTAMP,
        p_SoTien, 'Thành công'
    );

    COMMIT;
    p_TrangThai := 'Thành công';
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_thanh_toan;
/

-- 5.4 Hủy đặt vé và tính hoàn tiền
CREATE OR REPLACE PROCEDURE sp_huy_dat_ve (
    p_MaDatVe     IN  VARCHAR2,
    p_LyDo        IN  VARCHAR2 DEFAULT NULL,
    p_CoHoanTien  OUT NUMBER,
    p_SoTienHoan  OUT NUMBER
) AS
    v_TrangThai  VARCHAR2(30);
    v_ThoiGian   TIMESTAMP;
    v_TongTien   NUMBER;
    v_GioConLai  NUMBER;
    v_MaHoaDon   VARCHAR2(30);
BEGIN
    SELECT TrangThai
    INTO v_TrangThai
    FROM DATVE
    WHERE MaDatVe = p_MaDatVe;

    IF v_TrangThai IN ('Đã hủy','Hết hạn') THEN
        RAISE_APPLICATION_ERROR(-20040, 'Đơn đã hủy hoặc hết hạn.');
    END IF;

    -- Nếu chưa thanh toán thì chỉ hủy, không hoàn tiền
    IF v_TrangThai = 'Chờ thanh toán' THEN
        p_CoHoanTien := 0;
        p_SoTienHoan := 0;

        UPDATE DATVE
        SET TrangThai = 'Đã hủy'
        WHERE MaDatVe = p_MaDatVe;

        UPDATE HOADON
        SET TrangThai = 'Đã hủy'
        WHERE MaDatVe = p_MaDatVe;

        COMMIT;
        RETURN;
    END IF;

    -- Chỉ đơn đã thanh toán mới xét hoàn tiền
    IF v_TrangThai != 'Đã thanh toán' THEN
        RAISE_APPLICATION_ERROR(-20042,
            'Trạng thái đơn không hợp lệ để hủy.');
    END IF;

    SELECT MIN(cx.ThoiGianKhoiHanh)
    INTO v_ThoiGian
    FROM VE v
    JOIN CHUYENXE cx ON v.MaChuyen = cx.MaChuyen
    WHERE v.MaDatVe = p_MaDatVe;

    IF v_ThoiGian IS NULL THEN
        RAISE_APPLICATION_ERROR(-20043,
            'Đơn không có vé hợp lệ.');
    END IF;

    v_GioConLai := (CAST(v_ThoiGian AS DATE) - SYSDATE) * 24;

    SELECT MaHoaDon, NVL(TongTien,0)
    INTO v_MaHoaDon, v_TongTien
    FROM HOADON
    WHERE MaDatVe = p_MaDatVe;

    IF v_GioConLai > 24 THEN
        p_CoHoanTien := 1;
        p_SoTienHoan := v_TongTien;
    ELSIF v_GioConLai >= 2 THEN
        p_CoHoanTien := 1;
        p_SoTienHoan := ROUND(v_TongTien * 0.5);
    ELSE
        p_CoHoanTien := 0;
        p_SoTienHoan := 0;
    END IF;

    UPDATE DATVE
    SET TrangThai = 'Đã hủy'
    WHERE MaDatVe = p_MaDatVe;

    UPDATE HOADON
    SET TrangThai = 'Đã hủy'
    WHERE MaDatVe = p_MaDatVe;

    IF p_CoHoanTien = 1 AND p_SoTienHoan > 0 THEN
        INSERT INTO THANHTOAN (
            MaThanhToan, MaHoaDon, LoaiGiaoDich,
            PhuongThucThanhToan, NgayThanhToan,
            SoTien, TrangThai
        )
        VALUES (
            'HT' || LPAD(seq_thanhtoan.NEXTVAL, 8, '0'),
            v_MaHoaDon, 'HoanTien',
            'Hoàn tiền', SYSTIMESTAMP,
            p_SoTienHoan, 'Thành công'
        );
    END IF;

    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20041, 'Đơn đặt vé không tồn tại.');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_huy_dat_ve;
/

-- 5.5 Giải phóng ghế hết hạn giữ chỗ
CREATE OR REPLACE PROCEDURE sp_giai_phong_ghe_het_han (
    p_SoVeGiaiPhong OUT NUMBER
) AS
BEGIN
    UPDATE VE
    SET TrangThai = 'Đã hủy'
    WHERE TrangThai = 'Giữ chỗ'
      AND ThoiGianGiuDen < SYSTIMESTAMP;

    p_SoVeGiaiPhong := SQL%ROWCOUNT;

    UPDATE DATVE dv
    SET TrangThai = 'Hết hạn'
    WHERE dv.TrangThai = 'Chờ thanh toán'
      AND NOT EXISTS (
          SELECT 1
          FROM VE v
          WHERE v.MaDatVe = dv.MaDatVe
            AND v.TrangThai IN ('Giữ chỗ','Đã đặt','Đã dùng')
      );

    UPDATE HOADON hd
    SET TrangThai = 'Đã hủy'
    WHERE hd.TrangThai = 'Chưa thanh toán'
      AND EXISTS (
          SELECT 1
          FROM DATVE dv
          WHERE dv.MaDatVe = hd.MaDatVe
            AND dv.TrangThai = 'Hết hạn'
      );

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_giai_phong_ghe_het_han;
/

-- 5.6 Check-in vé QR
CREATE OR REPLACE PROCEDURE sp_kiem_tra_ve_qr (
    p_MaVe      IN  VARCHAR2,
    p_MaNV      IN  VARCHAR2,
    p_KetQua    OUT VARCHAR2,
    p_ThongTin  OUT VARCHAR2
) AS
    v_TrangThaiVe VARCHAR2(20);
    v_TrangThaiCX VARCHAR2(20);
    v_MaChuyen    VARCHAR2(20);
    v_SoGhe       VARCHAR2(10);
    v_TenKH       VARCHAR2(100);
    v_MaHangNV    VARCHAR2(20);
    v_MaHangCX    VARCHAR2(20);
BEGIN
    BEGIN
        SELECT nv.MaNhaXe
        INTO v_MaHangNV
        FROM NHANVIEN nv
        WHERE nv.MaNV = p_MaNV
          AND nv.TrangThai = 'Hoạt động';
    EXCEPTION WHEN NO_DATA_FOUND THEN
        p_KetQua := 'NhanVienKhongHopLe';
        p_ThongTin := 'Nhân viên không tồn tại hoặc không hoạt động.';
        RETURN;
    END;

    BEGIN
        SELECT v.TrangThai, cx.TrangThai, v.MaChuyen,
               g.SoGhe, kh.TenKH, x.MaNhaXe
        INTO v_TrangThaiVe, v_TrangThaiCX, v_MaChuyen,
             v_SoGhe, v_TenKH, v_MaHangCX
        FROM VE v
        JOIN GHE       g  ON v.MaGhe    = g.MaGhe
        JOIN DATVE     dv ON v.MaDatVe  = dv.MaDatVe
        JOIN KHACHHANG kh ON dv.MaKH    = kh.MaKH
        JOIN CHUYENXE  cx ON v.MaChuyen = cx.MaChuyen
        JOIN XE        x  ON cx.MaXe    = x.MaXe
        WHERE v.MaVe = p_MaVe;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        p_KetQua := 'KhongTonTai';
        p_ThongTin := 'Vé không tồn tại.';
        RETURN;
    END;

    IF v_MaHangNV != v_MaHangCX THEN
        p_KetQua := 'SaiHangXe';
        p_ThongTin := 'Nhân viên không thuộc hãng xe này.';
        RETURN;
    END IF;

    IF v_TrangThaiCX = 'Đã hủy' THEN
        p_KetQua := 'ChuyenDaHuy';
        p_ThongTin := 'Chuyến xe đã bị hủy.';
        RETURN;
    END IF;

    IF v_TrangThaiVe = 'Đã hủy' THEN
        p_KetQua := 'DaHuy';
        p_ThongTin := 'Vé đã bị hủy.';
        RETURN;
    ELSIF v_TrangThaiVe = 'Giữ chỗ' THEN
        p_KetQua := 'ChuaThanhToan';
        p_ThongTin := 'Vé chưa thanh toán.';
        RETURN;
    ELSIF v_TrangThaiVe = 'Đã dùng' THEN
        p_KetQua := 'DaDung';
        p_ThongTin := 'Vé đã được sử dụng.';
        RETURN;
    ELSIF v_TrangThaiVe != 'Đã đặt' THEN
        p_KetQua := 'KhongHopLe';
        p_ThongTin := 'Trạng thái vé không hợp lệ.';
        RETURN;
    END IF;

    UPDATE VE
    SET TrangThai = 'Đã dùng'
    WHERE MaVe = p_MaVe;

    COMMIT;

    p_KetQua := 'HopLe';
    p_ThongTin := 'KH: ' || v_TenKH ||
                  ' | Ghế: ' || v_SoGhe ||
                  ' | Chuyến: ' || v_MaChuyen;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_kiem_tra_ve_qr;
/

-- ============================================================
