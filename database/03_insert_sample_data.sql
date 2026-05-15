-- PHẦN 6: DỮ LIỆU MẪU
-- Dữ liệu chuyến/khuyến mãi dùng năm 2026 để còn hợp lệ khi demo.
-- Mật khẩu mẫu cho tất cả tài khoản là: 1
-- Hash dưới đây là BCrypt của chuỗi "1".
-- ============================================================

-- 6.1 Tài khoản 3 role: Admin, Nhân viên, Khách hàng
INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, Quyen, TrangThaiTK)
VALUES ('admin01', '$2a$10$0fl.WBekRMtI6JJcWqCc3eGjRpggF2SPTxWcpsw3ioQBSeN36CXFi', 'Admin', 'Hoạt động');

INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, Quyen, TrangThaiTK)
VALUES ('staff01', '$2a$10$0fl.WBekRMtI6JJcWqCc3eGjRpggF2SPTxWcpsw3ioQBSeN36CXFi', 'NhanVien', 'Hoạt động');

INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, Quyen, TrangThaiTK)
VALUES ('staff02', '$2a$10$0fl.WBekRMtI6JJcWqCc3eGjRpggF2SPTxWcpsw3ioQBSeN36CXFi', 'NhanVien', 'Hoạt động');

INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, Quyen, TrangThaiTK)
VALUES ('customer01', '$2a$10$0fl.WBekRMtI6JJcWqCc3eGjRpggF2SPTxWcpsw3ioQBSeN36CXFi', 'KhachHang', 'Hoạt động');

INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, Quyen, TrangThaiTK)
VALUES ('customer02', '$2a$10$0fl.WBekRMtI6JJcWqCc3eGjRpggF2SPTxWcpsw3ioQBSeN36CXFi', 'KhachHang', 'Hoạt động');

-- 6.2 Nhà xe
INSERT INTO NHAXE (
    MaNhaXe, TenNhaXe, SDT, Email, DiaChi, TrangThai, MoTa
)
VALUES (
    'NX01',
    'Phương Trang',
    '02838386852',
    'cskh@futabus.vn',
    '54 Trương Định, Q3, TP.HCM',
    'Hoạt động',
    'Hãng xe khách Phương Trang - FUTA Bus Lines'
);

INSERT INTO NHAXE (
    MaNhaXe, TenNhaXe, SDT, Email, DiaChi, TrangThai, MoTa
)
VALUES (
    'NX02',
    'Hoàng Long',
    '02438261234',
    'info@hoanglongbus.com',
    '7A Đinh Lễ, Hoàn Kiếm, Hà Nội',
    'Hoạt động',
    'Hãng xe khách Hoàng Long'
);

-- 6.3 Bến xe
INSERT INTO BENXE VALUES ('BEN01','Bến xe Miền Đông mới','292 Đinh Bộ Lĩnh, Bình Thạnh, TP.HCM');
INSERT INTO BENXE VALUES ('BEN02','Bến xe Miền Tây','395 Kinh Dương Vương, Bình Tân, TP.HCM');
INSERT INTO BENXE VALUES ('BEN03','Bến xe Đà Lạt','01 Tô Hiến Thành, P3, Đà Lạt');
INSERT INTO BENXE VALUES ('BEN04','Bến xe Nha Trang','58/4 Tháng 10, Vĩnh Hải, Nha Trang');
INSERT INTO BENXE VALUES ('BEN05','Bến xe Phan Thiết','Km 2, Quốc lộ 1A, Phan Thiết');
INSERT INTO BENXE VALUES ('BEN06','Bến xe Cần Thơ','91 Nguyễn Trãi, Ninh Kiều, Cần Thơ');
INSERT INTO BENXE VALUES ('B001','Hà Nội','Hà Nội');
INSERT INTO BENXE VALUES ('B016','Bắc Ninh','Tỉnh Bắc Ninh');
INSERT INTO BENXE VALUES ('BQT','Quảng Trị','Tỉnh Quảng Trị');

-- 6.4 Điểm mẫu theo bến: DIEMBEN
INSERT INTO DIEMBEN VALUES ('DB_BEN01_01','BEN01','Cổng chính Bến xe Miền Đông mới','292 Đinh Bộ Lĩnh, Bình Thạnh, TP.HCM','Cả hai',1,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_BEN01_02','BEN01','Khu vực nhà chờ Bến xe Miền Đông mới','Bến xe Miền Đông mới','Cả hai',2,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_BEN02_01','BEN02','Cổng chính Bến xe Miền Tây','395 Kinh Dương Vương, Bình Tân, TP.HCM','Cả hai',1,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_BEN02_02','BEN02','Khu vực quầy vé Bến xe Miền Tây','Bến xe Miền Tây','Cả hai',2,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_BEN03_01','BEN03','Cổng chính Bến xe Đà Lạt','01 Tô Hiến Thành, P3, Đà Lạt','Cả hai',1,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_BEN03_02','BEN03','Văn phòng trung chuyển Đà Lạt','Trung tâm Đà Lạt','Cả hai',2,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_B001_01','B001','Bến xe Mỹ Đình','Hà Nội','Cả hai',1,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_B001_02','B001','Bến xe Giáp Bát','Hà Nội','Cả hai',2,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_B001_03','B001','Bến xe Nước Ngầm','Hà Nội','Cả hai',3,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_B016_01','B016','Bến xe Bắc Ninh','Tỉnh Bắc Ninh','Cả hai',1,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_B016_02','B016','Ngã tư Võ Cường','Tỉnh Bắc Ninh','Cả hai',2,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_B016_03','B016','Từ Sơn','Tỉnh Bắc Ninh','Cả hai',3,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_BQT_01','BQT','Bến xe Đông Hà','Tỉnh Quảng Trị','Cả hai',1,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_BQT_02','BQT','Văn phòng Quảng Trị','Thành phố Đông Hà, Quảng Trị','Cả hai',2,'Hoạt động');
INSERT INTO DIEMBEN VALUES ('DB_BQT_03','BQT','Cầu vượt Đông Hà','Tỉnh Quảng Trị','Cả hai',3,'Hoạt động');

-- 6.5 Loại xe
INSERT INTO LOAIXE VALUES ('LX01','Limousine giường nằm 34 chỗ','Xe giường nằm cao cấp, có điều hòa, wifi, TV');
INSERT INTO LOAIXE VALUES ('LX02','Ghế ngồi 45 chỗ','Xe ghế ngồi tiêu chuẩn');
INSERT INTO LOAIXE VALUES ('LX03','Giường nằm VIP 22 chỗ','Xe giường nằm VIP, không gian rộng, tiện nghi cao cấp');

-- 6.6 Xe
INSERT INTO XE VALUES ('XE01','NX01','51B-123.45','LX01',34,'Hoạt động');
INSERT INTO XE VALUES ('XE02','NX01','51B-678.90','LX01',34,'Hoạt động');
INSERT INTO XE VALUES ('XE03','NX01','51C-111.22','LX03',22,'Hoạt động');
INSERT INTO XE VALUES ('XE04','NX02','29A-456.78','LX02',45,'Hoạt động');
INSERT INTO XE VALUES ('XE05','NX02','29B-999.00','LX01',34,'Bảo dưỡng');

-- 6.7 Ghế cho XE01, XE02: 34 ghế dạng T01..T17, D01..D17
BEGIN
    FOR i IN 1..17 LOOP
        INSERT INTO GHE VALUES ('G01-T' || LPAD(i,2,'0'), 'XE01', 'T' || LPAD(i,2,'0'));
        INSERT INTO GHE VALUES ('G01-D' || LPAD(i,2,'0'), 'XE01', 'D' || LPAD(i,2,'0'));
        INSERT INTO GHE VALUES ('G02-T' || LPAD(i,2,'0'), 'XE02', 'T' || LPAD(i,2,'0'));
        INSERT INTO GHE VALUES ('G02-D' || LPAD(i,2,'0'), 'XE02', 'D' || LPAD(i,2,'0'));
    END LOOP;
END;
/

-- Ghế cho XE03: 22 ghế VIP V01..V22
BEGIN
    FOR i IN 1..22 LOOP
        INSERT INTO GHE VALUES ('G03-V' || LPAD(i,2,'0'), 'XE03', 'V' || LPAD(i,2,'0'));
    END LOOP;
END;
/

-- Ghế cho XE04: 45 ghế dạng 1A..9E
BEGIN
    FOR hang IN 1..9 LOOP
        FOR cot IN 1..5 LOOP
            INSERT INTO GHE VALUES ('G04-' || hang || CHR(64+cot), 'XE04', hang || CHR(64+cot));
        END LOOP;
    END LOOP;
END;
/

-- 6.8 Tuyến xe
INSERT INTO TUYENXE VALUES ('TUY01','BEN01','BEN03',310,360);
INSERT INTO TUYENXE VALUES ('TUY02','BEN03','BEN01',310,360);
INSERT INTO TUYENXE VALUES ('TUY03','BEN01','BEN04',450,480);
INSERT INTO TUYENXE VALUES ('TUY04','BEN02','BEN06',170,210);
INSERT INTO TUYENXE VALUES ('TUY05','BEN06','BEN02',170,210);
INSERT INTO TUYENXE VALUES ('TUY06','BQT','B016',720,630);
INSERT INTO TUYENXE VALUES ('TUY07','B001','B016',35,60);

-- 6.9 Chuyến xe
INSERT INTO CHUYENXE VALUES ('CX0101','XE01','TUY01',TIMESTAMP '2026-07-20 07:00:00',TIMESTAMP '2026-07-20 13:00:00',250000,'Sắp chạy');
INSERT INTO CHUYENXE VALUES ('CX0102','XE02','TUY01',TIMESTAMP '2026-07-20 13:00:00',TIMESTAMP '2026-07-20 19:00:00',250000,'Sắp chạy');
INSERT INTO CHUYENXE VALUES ('CX0103','XE01','TUY02',TIMESTAMP '2026-07-21 07:00:00',TIMESTAMP '2026-07-21 13:00:00',250000,'Sắp chạy');
INSERT INTO CHUYENXE VALUES ('CX0201','XE04','TUY04',TIMESTAMP '2026-07-20 06:00:00',TIMESTAMP '2026-07-20 09:30:00',150000,'Sắp chạy');
INSERT INTO CHUYENXE VALUES ('CX006','XE01','TUY06',TIMESTAMP '2026-05-10 22:30:00',TIMESTAMP '2026-05-11 09:00:00',200000,'Sắp chạy');
INSERT INTO CHUYENXE VALUES ('CX_HNBN','XE04','TUY07',TIMESTAMP '2026-05-10 08:00:00',TIMESTAMP '2026-05-10 09:00:00',80000,'Đang mở bán');
INSERT INTO CHUYENXE VALUES ('CX_DONE','XE01','TUY01',TIMESTAMP '2026-04-01 07:00:00',TIMESTAMP '2026-04-01 13:00:00',250000,'Hoàn thành');

-- 6.10 Điểm đón/trả theo chuyến: copy từ DIEMBEN sang DIEMDONTRA
-- CX0101: Miền Đông mới -> Đà Lạt
INSERT INTO DIEMDONTRA VALUES ('DDT01','CX0101','DB_BEN01_01','BEN01','Cổng chính Bến xe Miền Đông mới',TIMESTAMP '2026-07-20 07:00:00','Đón',1);
INSERT INTO DIEMDONTRA VALUES ('DDT02','CX0101','DB_BEN01_02','BEN01','Khu vực nhà chờ Bến xe Miền Đông mới',TIMESTAMP '2026-07-20 06:45:00','Đón',2);
INSERT INTO DIEMDONTRA VALUES ('DDT03','CX0101','DB_BEN03_01','BEN03','Cổng chính Bến xe Đà Lạt',TIMESTAMP '2026-07-20 13:00:00','Trả',1);
INSERT INTO DIEMDONTRA VALUES ('DDT031','CX0101','DB_BEN03_02','BEN03','Văn phòng trung chuyển Đà Lạt',TIMESTAMP '2026-07-20 13:20:00','Trả',2);

-- CX0102
INSERT INTO DIEMDONTRA VALUES ('DDT07','CX0102','DB_BEN01_01','BEN01','Cổng chính Bến xe Miền Đông mới',TIMESTAMP '2026-07-20 13:00:00','Đón',1);
INSERT INTO DIEMDONTRA VALUES ('DDT08','CX0102','DB_BEN03_01','BEN03','Cổng chính Bến xe Đà Lạt',TIMESTAMP '2026-07-20 19:00:00','Trả',1);

-- CX0103: Đà Lạt -> Miền Đông mới
INSERT INTO DIEMDONTRA VALUES ('DDT09','CX0103','DB_BEN03_01','BEN03','Cổng chính Bến xe Đà Lạt',TIMESTAMP '2026-07-21 07:00:00','Đón',1);
INSERT INTO DIEMDONTRA VALUES ('DDT10','CX0103','DB_BEN01_01','BEN01','Cổng chính Bến xe Miền Đông mới',TIMESTAMP '2026-07-21 13:00:00','Trả',1);

-- CX0201: Miền Tây -> Cần Thơ
INSERT INTO DIEMDONTRA VALUES ('DDT04','CX0201','DB_BEN02_01','BEN02','Cổng chính Bến xe Miền Tây',TIMESTAMP '2026-07-20 06:00:00','Đón',1);
INSERT INTO DIEMDONTRA VALUES ('DDT05','CX0201','DB_BEN02_02','BEN02','Khu vực quầy vé Bến xe Miền Tây',TIMESTAMP '2026-07-20 05:45:00','Đón',2);
INSERT INTO DIEMDONTRA VALUES ('DDT06','CX0201',NULL,'BEN06','Bến xe Cần Thơ',TIMESTAMP '2026-07-20 09:30:00','Trả',1);

-- CX006: Quảng Trị -> Bắc Ninh, đúng dữ liệu để test search 2026-05-10
INSERT INTO DIEMDONTRA VALUES ('DD_CX006_01','CX006','DB_BQT_01','BQT','Bến xe Đông Hà',TIMESTAMP '2026-05-10 22:30:00','Đón',1);
INSERT INTO DIEMDONTRA VALUES ('DD_CX006_02','CX006','DB_BQT_02','BQT','Văn phòng Quảng Trị',TIMESTAMP '2026-05-10 22:10:00','Đón',2);
INSERT INTO DIEMDONTRA VALUES ('DT_CX006_01','CX006','DB_B016_01','B016','Bến xe Bắc Ninh',TIMESTAMP '2026-05-11 09:00:00','Trả',1);
INSERT INTO DIEMDONTRA VALUES ('DT_CX006_02','CX006','DB_B016_02','B016','Ngã tư Võ Cường',TIMESTAMP '2026-05-11 09:20:00','Trả',2);

-- CX_HNBN: Hà Nội -> Bắc Ninh
INSERT INTO DIEMDONTRA VALUES ('DD_HNBN_01','CX_HNBN','DB_B001_01','B001','Bến xe Mỹ Đình',TIMESTAMP '2026-05-10 08:00:00','Đón',1);
INSERT INTO DIEMDONTRA VALUES ('DD_HNBN_02','CX_HNBN','DB_B001_02','B001','Bến xe Giáp Bát',TIMESTAMP '2026-05-10 07:40:00','Đón',2);
INSERT INTO DIEMDONTRA VALUES ('DT_HNBN_01','CX_HNBN','DB_B016_01','B016','Bến xe Bắc Ninh',TIMESTAMP '2026-05-10 09:00:00','Trả',1);
INSERT INTO DIEMDONTRA VALUES ('DT_HNBN_02','CX_HNBN','DB_B016_03','B016','Từ Sơn',TIMESTAMP '2026-05-10 09:20:00','Trả',2);

-- CX_DONE
INSERT INTO DIEMDONTRA VALUES ('DDT_D1','CX_DONE','DB_BEN01_01','BEN01','Cổng chính Bến xe Miền Đông mới',TIMESTAMP '2026-04-01 07:00:00','Đón',1);
INSERT INTO DIEMDONTRA VALUES ('DDT_D2','CX_DONE','DB_BEN03_01','BEN03','Cổng chính Bến xe Đà Lạt',TIMESTAMP '2026-04-01 13:00:00','Trả',1);

-- 6.11 Loại vé
INSERT INTO LOAIVE VALUES ('LV01', N'Vé thường', 1, N'Vé tiêu chuẩn', N'Hoạt động');
INSERT INTO LOAIVE VALUES ('LV02', N'Vé VIP', 1.2, N'Vé cung cấp các dịch vu thêm', N'Hoạt động');

-- 6.12 Khuyến mãi
INSERT INTO KHUYENMAI VALUES ('KM01','Giảm 10% hè 2026',10,0,TIMESTAMP '2026-06-01 00:00:00',TIMESTAMP '2026-08-31 23:59:59','Đang áp dụng');
INSERT INTO KHUYENMAI VALUES ('KM02','Giảm 30.000đ ngày thường',0,30000,TIMESTAMP '2026-07-01 00:00:00',TIMESTAMP '2026-07-31 23:59:59','Đang áp dụng');

-- 6.13 Khách hàng
INSERT INTO KHACHHANG VALUES ('KH001','Nguyễn Văn A',DATE '1990-05-15','Nam','0901234567','nguyenvana@gmail.com',(SELECT MaTK FROM TAIKHOAN WHERE TenDangNhap='customer01'),'Hoạt động');
INSERT INTO KHACHHANG VALUES ('KH002','Trần Thị B',DATE '1995-08-22','Nữ','0912345678','tranthib@gmail.com',(SELECT MaTK FROM TAIKHOAN WHERE TenDangNhap='customer02'),'Hoạt động');

-- 6.14 Nhân viên
INSERT INTO NHANVIEN VALUES ('NV001','Trần Văn Minh','Nam','0934567890','minhnv@futabus.vn',DATE '2020-01-15','Hoạt động','NX01',(SELECT MaTK FROM TAIKHOAN WHERE TenDangNhap='staff01'));
INSERT INTO NHANVIEN VALUES ('NV002','Phạm Minh Tuấn','Nam','0956789012','tuannv@hoanglongbus.com',DATE '2019-03-10','Hoạt động','NX02',(SELECT MaTK FROM TAIKHOAN WHERE TenDangNhap='staff02'));

-- 6.15 Tiện ích
INSERT INTO TIENICH VALUES ('TI01','Wifi miễn phí');
INSERT INTO TIENICH VALUES ('TI02','Điều hòa nhiệt độ');
INSERT INTO TIENICH VALUES ('TI03','Nước uống miễn phí');
INSERT INTO TIENICH VALUES ('TI04','Chăn gối');
INSERT INTO TIENICH VALUES ('TI05','Cổng sạc USB');
INSERT INTO TIENICH VALUES ('TI06','TV giải trí');
INSERT INTO TIENICH VALUES ('TI07','Dây an toàn');
INSERT INTO TIENICH VALUES ('TI08','Búa phá kính');
INSERT INTO TIENICH VALUES ('TI09','Rèm cửa');

-- 6.16 Tiện ích xe
INSERT INTO TIENICHXE VALUES ('XE01','TI01');
INSERT INTO TIENICHXE VALUES ('XE01','TI02');
INSERT INTO TIENICHXE VALUES ('XE01','TI03');
INSERT INTO TIENICHXE VALUES ('XE01','TI04');
INSERT INTO TIENICHXE VALUES ('XE01','TI05');
INSERT INTO TIENICHXE VALUES ('XE01','TI07');
INSERT INTO TIENICHXE VALUES ('XE01','TI08');
INSERT INTO TIENICHXE VALUES ('XE01','TI09');
INSERT INTO TIENICHXE VALUES ('XE02','TI01');
INSERT INTO TIENICHXE VALUES ('XE02','TI02');
INSERT INTO TIENICHXE VALUES ('XE02','TI03');
INSERT INTO TIENICHXE VALUES ('XE04','TI02');
INSERT INTO TIENICHXE VALUES ('XE04','TI05');
INSERT INTO TIENICHXE VALUES ('XE04','TI07');

-- 6.17 Hình ảnh
INSERT INTO HINHANH VALUES ('ANH01','XE01','https://storage.futabus.vn/xe01_1.jpg',1);
INSERT INTO HINHANH VALUES ('ANH02','XE01','https://storage.futabus.vn/xe01_2.jpg',2);
INSERT INTO HINHANH VALUES ('ANH03','XE01','https://storage.futabus.vn/xe01_noidung.jpg',3);
INSERT INTO HINHANH VALUES ('ANH04','XE02','https://storage.futabus.vn/xe02_1.jpg',1);
INSERT INTO HINHANH VALUES ('ANH05','XE04','https://storage.hoanglongbus.com/xe04_1.jpg',1);

-- ============================================================
-- 6.18 Dữ liệu đơn đặt vé + vé + hóa đơn mẫu
-- ============================================================

-- Xóa dữ liệu test cũ nếu có
DELETE FROM THANHTOAN WHERE MaHoaDon IN ('HD_TEST01', 'HD_HOLD01');
DELETE FROM HOADON WHERE MaHoaDon IN ('HD_TEST01', 'HD_HOLD01');
DELETE FROM VE WHERE MaVe IN ('VE_TEST01', 'VE_HOLD01');
DELETE FROM DATVE WHERE MaDatVe IN ('DV_TEST01', 'DV_HOLD01');

COMMIT;

-- Đơn 1: KH001 đặt vé chuyến hoàn thành để test đánh giá
INSERT INTO DATVE (
    MaDatVe,
    MaKH,
    NgayDat,
    TrangThai
)
VALUES (
    'DV_TEST01',
    'KH001',
    TIMESTAMP '2026-04-01 00:00:00',
    'Đã thanh toán'
);

INSERT INTO VE (
    MaVe,
    MaDatVe,
    MaChuyen,
    MaGhe,
    MaKH,
    MaLoaiVe,
    MaDiemDon,
    MaDiemTra,
    GiaTien,
    TrangThai,
    ThoiGianDat,
    ThoiGianGiuDen
)
VALUES (
    'VE_TEST01',
    'DV_TEST01',
    'CX_DONE',
    'G01-T01',
    'KH001',
    'LV01',
    'DDT_D1',
    'DDT_D2',
    250000,
    N'Đã thanh toán',
    TIMESTAMP '2026-04-01 00:00:00',
    NULL
);

INSERT INTO HOADON (
    MaHoaDon,
    MaDatVe,
    NgayLap,
    GiaGoc,
    TienGiam,
    TongTien,
    MaKhuyenMai,
    TrangThai
)
VALUES (
    'HD_TEST01',
    'DV_TEST01',
    TIMESTAMP '2026-04-01 00:00:00',
    250000,
    0,
    250000,
    NULL,
    'Đã thanh toán'
);

INSERT INTO THANHTOAN (
    MaThanhToan,
    MaHoaDon,
    OrderCode,
    PayOSPaymentLinkId,
    CheckoutUrl,
    LoaiGiaoDich,
    PhuongThucThanhToan,
    SoTien,
    TrangThai,
    NgayTao,
    NgayThanhToan,
    NgayCapNhat
)
VALUES (
    'TT_TEST01',
    'HD_TEST01',
    100001,
    NULL,
    NULL,
    'ThanhToan',
    'PAYOS',
    250000,
    'Thành công',
    TIMESTAMP '2026-04-01 00:00:00',
    TIMESTAMP '2026-04-01 00:05:00',
    TIMESTAMP '2026-04-01 00:05:00'
);

-- Đơn 2: giữ chỗ trên CX006 để test PayOS
INSERT INTO DATVE (
    MaDatVe,
    MaKH,
    NgayDat,
    TrangThai
)
VALUES (
    'DV_HOLD01',
    'KH002',
    TIMESTAMP '2026-05-09 10:00:00',
    'Chờ thanh toán'
);

INSERT INTO VE (
    MaVe,
    MaDatVe,
    MaChuyen,
    MaGhe,
    MaKH,
    MaLoaiVe,
    MaDiemDon,
    MaDiemTra,
    GiaTien,
    TrangThai,
    ThoiGianDat,
    ThoiGianGiuDen
)
VALUES (
    'VE_HOLD01',
    'DV_HOLD01',
    'CX006',
    'G01-T02',
    'KH002',
    'LV01',
    'DD_CX006_01',
    'DT_CX006_01',
    200000,
    N'Giữ chỗ',
    TIMESTAMP '2026-05-09 10:00:00',
    TIMESTAMP '2026-05-10 21:00:00'
);

INSERT INTO HOADON (
    MaHoaDon,
    MaDatVe,
    NgayLap,
    GiaGoc,
    TienGiam,
    TongTien,
    MaKhuyenMai,
    TrangThai
)
VALUES (
    'HD_HOLD01',
    'DV_HOLD01',
    TIMESTAMP '2026-05-09 10:00:00',
    200000,
    0,
    200000,
    NULL,
    'Chưa thanh toán'
);

COMMIT;

-- 6.19 Đánh giá
INSERT INTO DANHGIA VALUES ('DG001','KH001','CX_DONE',5,'Xe sạch, tài xế chạy đúng giờ.',TIMESTAMP '2026-04-02 10:00:00');

COMMIT;

-- ============================================================
