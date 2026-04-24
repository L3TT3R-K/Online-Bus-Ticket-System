-- ============================================================
-- PHẦN 2: RÀNG BUỘC, INDEX, SEQUENCE
-- Ghi chú: Các PRIMARY KEY, FOREIGN KEY, UNIQUE, CHECK đã được khai báo inline trong 01_create_tables.sql.
-- File này chứa INDEX và SEQUENCE dùng cho trigger/procedure.
-- ============================================================

-- PHẦN 2: INDEX
-- ============================================================

CREATE INDEX idx_ve_datve        ON VE(MaDatVe);
CREATE INDEX idx_ve_chuyen       ON VE(MaChuyen);
CREATE INDEX idx_ve_trangthai    ON VE(TrangThai);
CREATE INDEX idx_ve_giu_den      ON VE(TrangThai, ThoiGianGiuDen);
CREATE INDEX idx_datve_kh        ON DATVE(MaKH);
CREATE INDEX idx_datve_tt        ON DATVE(TrangThai);
CREATE INDEX idx_chuyen_tuyen    ON CHUYENXE(MaTuyen);
CREATE INDEX idx_chuyen_tt       ON CHUYENXE(TrangThai);
CREATE INDEX idx_chuyen_khoi     ON CHUYENXE(ThoiGianKhoiHanh);
CREATE INDEX idx_tt_hoadon       ON THANHTOAN(MaHoaDon);
CREATE INDEX idx_dg_chuyen       ON DANHGIA(MaChuyen);
CREATE INDEX idx_ddt_chuyen      ON DIEMDONTRA(MaChuyen);
CREATE INDEX idx_ghe_xe          ON GHE(MaXe);

-- Unique index chỉ khóa ghế đang còn hiệu lực.
-- Vé Đã hủy không chặn khách khác đặt lại ghế.
CREATE UNIQUE INDEX uq_ve_chuyen_ghe_active
ON VE (
    CASE WHEN TrangThai IN ('Giữ chỗ','Đã đặt','Đã dùng') THEN MaChuyen END,
    CASE WHEN TrangThai IN ('Giữ chỗ','Đã đặt','Đã dùng') THEN MaGhe END
);

-- ============================================================

-- PHẦN 3: SEQUENCE
-- ============================================================

CREATE SEQUENCE seq_datve     START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_hoadon    START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_thanhtoan START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_ve_auto   START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_danhgia   START WITH 1 INCREMENT BY 1 NOCACHE;

-- Các sequence này giữ lại nếu muốn sinh mã KH/NV/XE/GHE ở ứng dụng.
CREATE SEQUENCE seq_makh      START WITH 1001 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_manv      START WITH 101  INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_maxe      START WITH 1    INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_maghe     START WITH 1    INCREMENT BY 1 NOCACHE;

-- ============================================================
