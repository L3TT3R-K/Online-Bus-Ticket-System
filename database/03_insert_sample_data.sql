-- PHẦN 6: DỮ LIỆU MẪU
-- Dữ liệu chuyến/khuyến mãi dùng năm 2026 để còn hợp lệ khi demo.
-- ============================================================

-- 6.1 Tài khoản
INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, Quyen, TrangThaiTK)
VALUES ('admin01', 'hashed_admin_pw', 'Admin', 'Hoạt động');

INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, Quyen, TrangThaiTK)
VALUES ('phuongtrang_nv01', 'hashed_pw1', 'NhanVien', 'Hoạt động');

INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, Quyen, TrangThaiTK)
VALUES ('phuongtrang_nv02', 'hashed_pw2', 'NhanVien', 'Hoạt động');

INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, Quyen, TrangThaiTK)
VALUES ('hoanlong_nv01', 'hashed_pw3', 'NhanVien', 'Hoạt động');

INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, Quyen, TrangThaiTK)
VALUES ('kh_nguyenvana', 'hashed_pw5', 'KhachHang', 'Hoạt động');

INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, Quyen, TrangThaiTK)
VALUES ('kh_tranthib', 'hashed_pw6', 'KhachHang', 'Hoạt động');

INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, Quyen, TrangThaiTK)
VALUES ('kh_lethic', 'hashed_pw7', 'KhachHang', 'Hoạt động');

-- 6.2 Hãng xe
INSERT INTO NHAXE VALUES ('NX01','Phương Trang','02838386852',
    'cskh@futabus.vn','54 Trương Định, Q3, TP.HCM',
    'Hãng xe khách Phương Trang - FUTA Bus Lines');

INSERT INTO NHAXE VALUES ('NX02','Hoàng Long','02438261234',
    'info@hoanglongbus.com','7A Đinh Lễ, Hoàn Kiếm, Hà Nội',
    'Hãng xe khách Hoàng Long');

-- 6.3 Bến xe
INSERT INTO BENXE VALUES ('BEN01','Bến xe Miền Đông mới',
    '292 Đinh Bộ Lĩnh, Bình Thạnh, TP.HCM');

INSERT INTO BENXE VALUES ('BEN02','Bến xe Miền Tây',
    '395 Kinh Dương Vương, Bình Tân, TP.HCM');

INSERT INTO BENXE VALUES ('BEN03','Bến xe Đà Lạt',
    '01 Tô Hiến Thành, P3, Đà Lạt');

INSERT INTO BENXE VALUES ('BEN04','Bến xe Nha Trang',
    '58/4 Tháng 10, Vĩnh Hải, Nha Trang');

INSERT INTO BENXE VALUES ('BEN05','Bến xe Phan Thiết',
    'Km 2, Quốc lộ 1A, Phan Thiết');

INSERT INTO BENXE VALUES ('BEN06','Bến xe Cần Thơ',
    '91 Nguyễn Trãi, Ninh Kiều, Cần Thơ');

-- 6.4 Loại xe
INSERT INTO LOAIXE VALUES ('LX01','Limousine giường nằm 34 chỗ',
    'Xe giường nằm cao cấp, có điều hòa, wifi, TV');

INSERT INTO LOAIXE VALUES ('LX02','Ghế ngồi 45 chỗ',
    'Xe ghế ngồi tiêu chuẩn');

INSERT INTO LOAIXE VALUES ('LX03','Giường nằm VIP 22 chỗ',
    'Xe giường nằm VIP, không gian rộng, tiện nghi cao cấp');

-- 6.5 Xe
INSERT INTO XE VALUES ('XE01','NX01','51B-123.45','LX01',34,'Hoạt động');
INSERT INTO XE VALUES ('XE02','NX01','51B-678.90','LX01',34,'Hoạt động');
INSERT INTO XE VALUES ('XE03','NX01','51C-111.22','LX03',22,'Hoạt động');
INSERT INTO XE VALUES ('XE04','NX02','29A-456.78','LX02',45,'Hoạt động');
INSERT INTO XE VALUES ('XE05','NX02','29B-999.00','LX01',34,'Bảo dưỡng');

-- 6.6 Ghế cho XE01
BEGIN
    FOR i IN 1..17 LOOP
        INSERT INTO GHE VALUES (
            'G01-T' || LPAD(i,2,'0'),
            'XE01',
            'T' || LPAD(i,2,'0')
        );

        INSERT INTO GHE VALUES (
            'G01-D' || LPAD(i,2,'0'),
            'XE01',
            'D' || LPAD(i,2,'0')
        );
    END LOOP;
END;
/

-- Ghế cho XE02
BEGIN
    FOR i IN 1..17 LOOP
        INSERT INTO GHE VALUES (
            'G02-T' || LPAD(i,2,'0'),
            'XE02',
            'T' || LPAD(i,2,'0')
        );

        INSERT INTO GHE VALUES (
            'G02-D' || LPAD(i,2,'0'),
            'XE02',
            'D' || LPAD(i,2,'0')
        );
    END LOOP;
END;
/

-- Ghế cho XE04
BEGIN
    FOR hang IN 1..9 LOOP
        FOR cot IN 1..5 LOOP
            INSERT INTO GHE VALUES (
                'G04-' || hang || CHR(64+cot),
                'XE04',
                hang || CHR(64+cot)
            );
        END LOOP;
    END LOOP;
END;
/

-- 6.7 Tuyến xe
INSERT INTO TUYENXE VALUES ('TUY01','BEN01','BEN03',310,360);
INSERT INTO TUYENXE VALUES ('TUY02','BEN03','BEN01',310,360);
INSERT INTO TUYENXE VALUES ('TUY03','BEN01','BEN04',450,480);
INSERT INTO TUYENXE VALUES ('TUY04','BEN02','BEN06',170,210);
INSERT INTO TUYENXE VALUES ('TUY05','BEN06','BEN02',170,210);

-- 6.8 Chuyến xe
INSERT INTO CHUYENXE VALUES (
    'CX0101','XE01','TUY01',
    TIMESTAMP '2026-07-20 07:00:00',
    TIMESTAMP '2026-07-20 13:00:00',
    250000,'Sắp chạy'
);

INSERT INTO CHUYENXE VALUES (
    'CX0102','XE02','TUY01',
    TIMESTAMP '2026-07-20 13:00:00',
    TIMESTAMP '2026-07-20 19:00:00',
    250000,'Sắp chạy'
);

INSERT INTO CHUYENXE VALUES (
    'CX0103','XE01','TUY02',
    TIMESTAMP '2026-07-21 07:00:00',
    TIMESTAMP '2026-07-21 13:00:00',
    250000,'Sắp chạy'
);

INSERT INTO CHUYENXE VALUES (
    'CX0201','XE04','TUY04',
    TIMESTAMP '2026-07-20 06:00:00',
    TIMESTAMP '2026-07-20 09:30:00',
    150000,'Sắp chạy'
);

-- Chuyến đã hoàn thành để test đánh giá
INSERT INTO CHUYENXE VALUES (
    'CX_DONE','XE01','TUY01',
    TIMESTAMP '2026-04-01 07:00:00',
    TIMESTAMP '2026-04-01 13:00:00',
    250000,'Hoàn thành'
);

-- 6.9 Điểm đón/trả
-- Chuyến CX0101
INSERT INTO DIEMDONTRA VALUES ('DDT01','CX0101',
    'Bến xe Miền Đông mới',
    TIMESTAMP '2026-07-20 07:00:00','Đón',1);

INSERT INTO DIEMDONTRA VALUES ('DDT02','CX0101',
    'Trạm dừng Bảo Lộc - Quán Cafe Bảo Lâm',
    TIMESTAMP '2026-07-20 10:30:00','Đón',2);

INSERT INTO DIEMDONTRA VALUES ('DDT03','CX0101',
    'Bến xe Đà Lạt',
    TIMESTAMP '2026-07-20 13:00:00','Trả',3);

-- Chuyến CX0102
INSERT INTO DIEMDONTRA VALUES ('DDT07','CX0102',
    'Bến xe Miền Đông mới',
    TIMESTAMP '2026-07-20 13:00:00','Đón',1);

INSERT INTO DIEMDONTRA VALUES ('DDT08','CX0102',
    'Bến xe Đà Lạt',
    TIMESTAMP '2026-07-20 19:00:00','Trả',2);

-- Chuyến CX0103
INSERT INTO DIEMDONTRA VALUES ('DDT09','CX0103',
    'Bến xe Đà Lạt',
    TIMESTAMP '2026-07-21 07:00:00','Đón',1);

INSERT INTO DIEMDONTRA VALUES ('DDT10','CX0103',
    'Bến xe Miền Đông mới',
    TIMESTAMP '2026-07-21 13:00:00','Trả',2);

-- Chuyến CX0201
INSERT INTO DIEMDONTRA VALUES ('DDT04','CX0201',
    'Bến xe Miền Tây',
    TIMESTAMP '2026-07-20 06:00:00','Đón',1);

INSERT INTO DIEMDONTRA VALUES ('DDT05','CX0201',
    'Điểm đón Bình Chánh',
    TIMESTAMP '2026-07-20 06:30:00','Đón',2);

INSERT INTO DIEMDONTRA VALUES ('DDT06','CX0201',
    'Bến xe Cần Thơ',
    TIMESTAMP '2026-07-20 09:30:00','Trả',3);

-- Chuyến CX_DONE
INSERT INTO DIEMDONTRA VALUES ('DDT_D1','CX_DONE',
    'Bến xe Miền Đông mới',
    TIMESTAMP '2026-04-01 07:00:00','Đón',1);

INSERT INTO DIEMDONTRA VALUES ('DDT_D2','CX_DONE',
    'Bến xe Đà Lạt',
    TIMESTAMP '2026-04-01 13:00:00','Trả',2);

-- 6.10 Loại vé
INSERT INTO LOAIVE VALUES ('LV01','Vé giường thường');
INSERT INTO LOAIVE VALUES ('LV02','Vé giường VIP');
INSERT INTO LOAIVE VALUES ('LV03','Vé giường cuối xe');
INSERT INTO LOAIVE VALUES ('LV04','Vé ghế ngồi');

-- 6.11 Khuyến mãi
INSERT INTO KHUYENMAI VALUES (
    'KM01','Giảm 10% hè 2026',
    10, 0,
    TIMESTAMP '2026-06-01 00:00:00',
    TIMESTAMP '2026-08-31 23:59:59',
    'Đang áp dụng'
);

INSERT INTO KHUYENMAI VALUES (
    'KM02','Giảm 30.000đ ngày thường',
    0, 30000,
    TIMESTAMP '2026-07-01 00:00:00',
    TIMESTAMP '2026-07-31 23:59:59',
    'Đang áp dụng'
);

-- 6.12 Khách hàng
INSERT INTO KHACHHANG VALUES (
    'KH001','Nguyễn Văn A',DATE '1990-05-15','Nam',
    '0901234567','nguyenvana@gmail.com',
    (SELECT MaTK FROM TAIKHOAN WHERE TenDangNhap='kh_nguyenvana'),
    'Hoạt động'
);

INSERT INTO KHACHHANG VALUES (
    'KH002','Trần Thị B',DATE '1995-08-22','Nữ',
    '0912345678','tranthib@gmail.com',
    (SELECT MaTK FROM TAIKHOAN WHERE TenDangNhap='kh_tranthib'),
    'Hoạt động'
);

INSERT INTO KHACHHANG VALUES (
    'KH003','Lê Thị C',DATE '1988-12-30','Nữ',
    '0923456789','lethic@gmail.com',
    (SELECT MaTK FROM TAIKHOAN WHERE TenDangNhap='kh_lethic'),
    'Hoạt động'
);

-- 6.13 Nhân viên
INSERT INTO NHANVIEN VALUES (
    'NV001','Trần Văn Minh','Nam','0934567890',
    'minhnv@futabus.vn',DATE '2020-01-15',
    'Hoạt động','NX01',
    (SELECT MaTK FROM TAIKHOAN WHERE TenDangNhap='phuongtrang_nv01')
);

INSERT INTO NHANVIEN VALUES (
    'NV002','Nguyễn Thị Hoa','Nữ','0945678901',
    'hoanv@futabus.vn',DATE '2021-06-01',
    'Hoạt động','NX01',
    (SELECT MaTK FROM TAIKHOAN WHERE TenDangNhap='phuongtrang_nv02')
);

INSERT INTO NHANVIEN VALUES (
    'NV003','Phạm Minh Tuấn','Nam','0956789012',
    'tuannv@hoanglongbus.com',DATE '2019-03-10',
    'Hoạt động','NX02',
    (SELECT MaTK FROM TAIKHOAN WHERE TenDangNhap='hoanlong_nv01')
);

-- 6.14 Tiện ích
INSERT INTO TIENICH VALUES ('TI01','Wifi miễn phí');
INSERT INTO TIENICH VALUES ('TI02','Điều hòa nhiệt độ');
INSERT INTO TIENICH VALUES ('TI03','Nước uống miễn phí');
INSERT INTO TIENICH VALUES ('TI04','Chăn gối');
INSERT INTO TIENICH VALUES ('TI05','Cổng sạc USB');
INSERT INTO TIENICH VALUES ('TI06','TV giải trí');

-- 6.15 Tiện ích xe
INSERT INTO TIENICHXE VALUES ('XE01','TI01');
INSERT INTO TIENICHXE VALUES ('XE01','TI02');
INSERT INTO TIENICHXE VALUES ('XE01','TI03');
INSERT INTO TIENICHXE VALUES ('XE01','TI04');
INSERT INTO TIENICHXE VALUES ('XE01','TI05');
INSERT INTO TIENICHXE VALUES ('XE02','TI01');
INSERT INTO TIENICHXE VALUES ('XE02','TI02');
INSERT INTO TIENICHXE VALUES ('XE02','TI03');
INSERT INTO TIENICHXE VALUES ('XE04','TI02');
INSERT INTO TIENICHXE VALUES ('XE04','TI05');

-- 6.16 Hình ảnh
INSERT INTO HINHANH VALUES ('ANH01','XE01',
    'https://storage.futabus.vn/xe01_1.jpg',1);

INSERT INTO HINHANH VALUES ('ANH02','XE01',
    'https://storage.futabus.vn/xe01_2.jpg',2);

INSERT INTO HINHANH VALUES ('ANH03','XE01',
    'https://storage.futabus.vn/xe01_noidung.jpg',3);

INSERT INTO HINHANH VALUES ('ANH04','XE02',
    'https://storage.futabus.vn/xe02_1.jpg',1);

-- 6.17 Dữ liệu đơn đặt vé + vé + hóa đơn mẫu
-- Đơn 1: KH001 đặt vé chuyến hoàn thành để test đánh giá
INSERT INTO DATVE VALUES (
    'DV_TEST01','KH001',
    TIMESTAMP '2026-04-01 00:00:00',
    'Đã thanh toán'
);

INSERT INTO VE VALUES (
    'VE_TEST01','DV_TEST01','CX_DONE',
    'G01-T01','LV01','DDT_D1','DDT_D2',
    'Đã đặt',NULL
);

INSERT INTO HOADON (
    MaHoaDon, MaDatVe, NgayLap,
    GiaGoc, TienGiam, TongTien,
    MaKhuyenMai, TrangThai
)
VALUES (
    'HD_TEST01','DV_TEST01',
    TIMESTAMP '2026-04-01 00:00:00',
    250000,0,250000,NULL,'Đã thanh toán'
);

INSERT INTO THANHTOAN (
    MaThanhToan, MaHoaDon, LoaiGiaoDich,
    PhuongThucThanhToan, NgayThanhToan,
    SoTien, TrangThai
)
VALUES (
    'TT_TEST01','HD_TEST01','ThanhToan',
    'VNPay',
    TIMESTAMP '2026-04-01 00:05:00',
    250000,'Thành công'
);

COMMIT;

-- ============================================================
