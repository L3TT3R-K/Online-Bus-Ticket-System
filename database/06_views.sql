CREATE OR REPLACE VIEW V_DANHGIA_HANG_XE AS
SELECT
    nx.MaNhaXe,
    nx.TenNhaXe,
    COUNT(dg.MaDanhGia) AS SoLuotDanhGia,
    ROUND(AVG(dg.SoSao), 1) AS DiemTrungBinh,
    SUM(CASE WHEN dg.SoSao = 5 THEN 1 ELSE 0 END) AS SoSao5,
    SUM(CASE WHEN dg.SoSao = 4 THEN 1 ELSE 0 END) AS SoSao4,
    SUM(CASE WHEN dg.SoSao = 3 THEN 1 ELSE 0 END) AS SoSao3,
    SUM(CASE WHEN dg.SoSao <= 2 THEN 1 ELSE 0 END) AS SoSaoDuoi3
FROM DANHGIA dg
JOIN CHUYENXE cx ON dg.MaChuyen = cx.MaChuyen
JOIN XE xe ON cx.MaXe = xe.MaXe
JOIN NHAXE nx ON xe.MaNhaXe = nx.MaNhaXe
GROUP BY nx.MaNhaXe, nx.TenNhaXe;

CREATE OR REPLACE VIEW V_DANH_SACH_CHUYEN AS
SELECT
    cx.MaChuyen,
    nx.MaNhaXe,
    nx.TenNhaXe,
    bd.TenBen AS BenDi,
    bden.TenBen AS BenDen,
    cx.ThoiGianKhoiHanh,
    cx.ThoiGianDen,
    cx.GiaVe,
    cx.TrangThai,
    xe.MaXe,
    xe.BienSo,
    xe.SoLuongGhe,
    lx.TenLoaiXe,
    cx.ThoiGianDen - cx.ThoiGianKhoiHanh AS ThoiGianDiChuyen,
    (
        SELECT COUNT(*)
        FROM VE v
        WHERE v.MaChuyen = cx.MaChuyen
          AND v.TrangThai IN ('Giữ chỗ', 'Đã đặt', 'Đã thanh toán', 'Đã dùng')
    ) AS SoGheDaDat,
    xe.SoLuongGhe - (
        SELECT COUNT(*)
        FROM VE v
        WHERE v.MaChuyen = cx.MaChuyen
          AND v.TrangThai IN ('Giữ chỗ', 'Đã đặt', 'Đã thanh toán', 'Đã dùng')
    ) AS SoGheTrong,
    (
        SELECT LISTAGG(ha.Url, '||') WITHIN GROUP (ORDER BY ha.ThuTu)
        FROM HINHANH ha
        WHERE ha.MaXe = xe.MaXe
    ) AS ImageUrls,
    (
        SELECT LISTAGG(ti.TenTienIch, '||') WITHIN GROUP (ORDER BY ti.TenTienIch)
        FROM TIENICHXE tix
        JOIN TIENICH ti ON tix.MaTienIch = ti.MaTienIch
        WHERE tix.MaXe = xe.MaXe
    ) AS Amenities,
    NVL(dgx.DiemTrungBinh, 0) AS Rating,
    NVL(dgx.SoLuotDanhGia, 0) AS ReviewCount
FROM CHUYENXE cx
JOIN XE xe ON cx.MaXe = xe.MaXe
JOIN NHAXE nx ON xe.MaNhaXe = nx.MaNhaXe
JOIN LOAIXE lx ON xe.MaLoaiXe = lx.MaLoaiXe
JOIN TUYENXE t ON cx.MaTuyen = t.MaTuyen
JOIN BENXE bd ON t.MaBenDi = bd.MaBen
JOIN BENXE bden ON t.MaBenDen = bden.MaBen
LEFT JOIN V_DANHGIA_HANG_XE dgx ON dgx.MaNhaXe = nx.MaNhaXe;

CREATE OR REPLACE VIEW V_LICH_SU_DAT_VE AS
SELECT
    dv.MaDatVe,
    kh.TenKH,
    kh.SDT,
    kh.Email,
    cx.MaChuyen,
    bd.TenBen AS BenDi,
    bden.TenBen AS BenDen,
    cx.ThoiGianKhoiHanh,
    g.SoGhe,
    lv.TenLoaiVe,
    ddon.TenDiem AS DiemDon,
    dtra.TenDiem AS DiemTra,
    hd.TongTien,
    hd.TrangThai AS TrangThaiHoaDon,
    dv.TrangThai AS TrangThaiDon,
    dv.NgayDat,
    v.TrangThai AS TrangThaiVe
FROM DATVE dv
JOIN KHACHHANG kh ON dv.MaKH = kh.MaKH
JOIN VE v ON v.MaDatVe = dv.MaDatVe
JOIN CHUYENXE cx ON v.MaChuyen = cx.MaChuyen
JOIN GHE g ON v.MaGhe = g.MaGhe
JOIN LOAIVE lv ON v.MaLoaiVe = lv.MaLoaiVe
JOIN DIEMDONTRA ddon ON v.MaDiemDon = ddon.MaDiem
JOIN DIEMDONTRA dtra ON v.MaDiemTra = dtra.MaDiem
JOIN TUYENXE t ON cx.MaTuyen = t.MaTuyen
JOIN BENXE bd ON t.MaBenDi = bd.MaBen
JOIN BENXE bden ON t.MaBenDen = bden.MaBen
LEFT JOIN HOADON hd ON hd.MaDatVe = dv.MaDatVe;

CREATE OR REPLACE VIEW V_DOANH_THU_HANG_XE AS
WITH don_theo_hang AS (
    SELECT
        hd.MaHoaDon,
        hd.MaDatVe,
        hd.NgayLap,
        hd.TongTien,
        nx.MaNhaXe,
        nx.TenNhaXe,
        COUNT(v.MaVe) AS SoVeTrongDon,
        COUNT(DISTINCT cx.MaChuyen) AS SoChuyenTrongDon
    FROM HOADON hd
    JOIN DATVE dv ON hd.MaDatVe = dv.MaDatVe
    JOIN VE v ON v.MaDatVe = dv.MaDatVe
    JOIN CHUYENXE cx ON v.MaChuyen = cx.MaChuyen
    JOIN XE xe ON cx.MaXe = xe.MaXe
    JOIN NHAXE nx ON xe.MaNhaXe = nx.MaNhaXe
    WHERE hd.TrangThai = 'Đã thanh toán'
    GROUP BY
        hd.MaHoaDon,
        hd.MaDatVe,
        hd.NgayLap,
        hd.TongTien,
        nx.MaNhaXe,
        nx.TenNhaXe
)
SELECT
    MaNhaXe,
    TenNhaXe,
    TRUNC(NgayLap, 'MM') AS Thang,
    COUNT(DISTINCT MaDatVe) AS SoDon,
    SUM(SoVeTrongDon) AS SoVe,
    SUM(SoChuyenTrongDon) AS SoChuyen,
    SUM(TongTien) AS TongDoanhThu
FROM don_theo_hang
GROUP BY MaNhaXe, TenNhaXe, TRUNC(NgayLap, 'MM');

CREATE OR REPLACE VIEW V_DOANH_THU_HANG_XE_TONG_HOP AS
WITH chuyen_theo_hang AS (
    SELECT
        x.MaNhaXe,
        COUNT(DISTINCT c.MaChuyen) AS TripCount
    FROM XE x
    JOIN CHUYENXE c ON x.MaXe = c.MaXe
    GROUP BY x.MaNhaXe
),
hoa_don_theo_hang AS (
    SELECT
        h.MaHoaDon,
        h.TongTien,
        nx.MaNhaXe
    FROM HOADON h
    JOIN DATVE dv ON h.MaDatVe = dv.MaDatVe
    JOIN VE v ON v.MaDatVe = dv.MaDatVe
    JOIN CHUYENXE c ON v.MaChuyen = c.MaChuyen
    JOIN XE x ON c.MaXe = x.MaXe
    JOIN NHAXE nx ON x.MaNhaXe = nx.MaNhaXe
    WHERE h.TrangThai = 'Đã thanh toán'
    GROUP BY h.MaHoaDon, h.TongTien, nx.MaNhaXe
),
doanh_thu_theo_hang AS (
    SELECT
        MaNhaXe,
        COUNT(DISTINCT MaHoaDon) AS PaidOrderCount,
        SUM(TongTien) AS Revenue
    FROM hoa_don_theo_hang
    GROUP BY MaNhaXe
)
SELECT
    nx.MaNhaXe,
    nx.TenNhaXe,
    NVL(cth.TripCount, 0) AS TripCount,
    NVL(dth.PaidOrderCount, 0) AS PaidOrderCount,
    NVL(dth.Revenue, 0) AS Revenue
FROM NHAXE nx
LEFT JOIN chuyen_theo_hang cth ON cth.MaNhaXe = nx.MaNhaXe
LEFT JOIN doanh_thu_theo_hang dth ON dth.MaNhaXe = nx.MaNhaXe;

COMMIT;
