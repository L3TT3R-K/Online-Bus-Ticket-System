-- PHẦN 8: VIEWS HỮU ÍCH
-- ============================================================

-- 8.1 Danh sách chuyến xe kèm thông tin đầy đủ
CREATE OR REPLACE VIEW v_danh_sach_chuyen AS
SELECT
    cx.MaChuyen,
    nx.TenNhaXe,
    bd.TenBen   AS BenDi,
    bden.TenBen AS BenDen,
    cx.ThoiGianKhoiHanh,
    cx.ThoiGianDen,
    cx.GiaVe,
    cx.TrangThai,
    xe.BienSo,
    lx.TenLoaiXe,
    cx.ThoiGianDen - cx.ThoiGianKhoiHanh AS ThoiGianDiChuyen,
    (
        SELECT COUNT(*)
        FROM VE v
        WHERE v.MaChuyen = cx.MaChuyen
          AND v.TrangThai IN ('Giữ chỗ','Đã đặt','Đã dùng')
    ) AS SoGheDaDat,
    xe.SoLuongGhe - (
        SELECT COUNT(*)
        FROM VE v
        WHERE v.MaChuyen = cx.MaChuyen
          AND v.TrangThai IN ('Giữ chỗ','Đã đặt','Đã dùng')
    ) AS SoGheTrong
FROM CHUYENXE cx
JOIN XE       xe   ON cx.MaXe     = xe.MaXe
JOIN NHAXE    nx   ON xe.MaNhaXe  = nx.MaNhaXe
JOIN LOAIXE   lx   ON xe.MaLoaiXe = lx.MaLoaiXe
JOIN TUYENXE  t    ON cx.MaTuyen  = t.MaTuyen
JOIN BENXE    bd   ON t.MaBenDi   = bd.MaBen
JOIN BENXE    bden ON t.MaBenDen  = bden.MaBen;

-- 8.2 Lịch sử đặt vé của khách hàng
CREATE OR REPLACE VIEW v_lich_su_dat_ve AS
SELECT
    dv.MaDatVe,
    kh.TenKH,
    kh.SDT,
    kh.Email,
    cx.MaChuyen,
    bd.TenBen     AS BenDi,
    bden.TenBen   AS BenDen,
    cx.ThoiGianKhoiHanh,
    g.SoGhe,
    lv.TenLoaiVe,
    hd.TongTien,
    hd.TrangThai  AS TrangThaiHoaDon,
    dv.TrangThai  AS TrangThaiDon,
    dv.NgayDat,
    v.TrangThai   AS TrangThaiVe
FROM DATVE dv
JOIN KHACHHANG kh  ON dv.MaKH      = kh.MaKH
JOIN VE        v   ON v.MaDatVe    = dv.MaDatVe
JOIN CHUYENXE  cx  ON v.MaChuyen   = cx.MaChuyen
JOIN GHE       g   ON v.MaGhe      = g.MaGhe
JOIN LOAIVE    lv  ON v.MaLoaiVe   = lv.MaLoaiVe
JOIN TUYENXE   t   ON cx.MaTuyen   = t.MaTuyen
JOIN BENXE     bd  ON t.MaBenDi    = bd.MaBen
JOIN BENXE     bden ON t.MaBenDen  = bden.MaBen
LEFT JOIN HOADON hd ON hd.MaDatVe  = dv.MaDatVe;

-- 8.3 Doanh thu theo hãng xe
-- Tính theo hóa đơn trước để tránh nhân doanh thu khi một đơn có nhiều vé.
CREATE OR REPLACE VIEW v_doanh_thu_hang_xe AS
WITH don_theo_hang AS (
    SELECT
        hd.MaHoaDon,
        hd.MaDatVe,
        hd.NgayLap,
        hd.TongTien,
        nx.MaNhaXe,
        nx.TenNhaXe,
        COUNT(v.MaVe) AS SoVeTrongDon
    FROM HOADON hd
    JOIN DATVE    dv ON hd.MaDatVe   = dv.MaDatVe
    JOIN VE       v  ON v.MaDatVe    = dv.MaDatVe
    JOIN CHUYENXE cx ON v.MaChuyen   = cx.MaChuyen
    JOIN XE       xe ON cx.MaXe      = xe.MaXe
    JOIN NHAXE    nx ON xe.MaNhaXe   = nx.MaNhaXe
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
    TRUNC(NgayLap,'MM') AS Thang,
    COUNT(DISTINCT MaDatVe) AS SoDon,
    SUM(SoVeTrongDon) AS SoVe,
    SUM(TongTien) AS TongDoanhThu
FROM don_theo_hang
GROUP BY MaNhaXe, TenNhaXe, TRUNC(NgayLap,'MM');

-- 8.4 Đánh giá trung bình theo hãng xe
CREATE OR REPLACE VIEW v_danhgia_hang_xe AS
SELECT
    nx.MaNhaXe,
    nx.TenNhaXe,
    COUNT(dg.MaDanhGia) AS SoLuotDanhGia,
    ROUND(AVG(dg.SoSao),1) AS DiemTrungBinh,
    SUM(CASE WHEN dg.SoSao = 5 THEN 1 ELSE 0 END) AS SoSao5,
    SUM(CASE WHEN dg.SoSao = 4 THEN 1 ELSE 0 END) AS SoSao4,
    SUM(CASE WHEN dg.SoSao = 3 THEN 1 ELSE 0 END) AS SoSao3,
    SUM(CASE WHEN dg.SoSao <= 2 THEN 1 ELSE 0 END) AS SoSaoDuoi3
FROM DANHGIA  dg
JOIN CHUYENXE cx ON dg.MaChuyen = cx.MaChuyen
JOIN XE       xe ON cx.MaXe     = xe.MaXe
JOIN NHAXE    nx ON xe.MaNhaXe  = nx.MaNhaXe
GROUP BY nx.MaNhaXe, nx.TenNhaXe;

COMMIT;

-- ============================================================
