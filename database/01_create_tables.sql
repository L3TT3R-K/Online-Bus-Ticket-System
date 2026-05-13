-- PHẦN 0: DỌN DẸP ĐỐI TƯỢNG CŨ
-- ============================================================

-- Drop scheduler job nếu đã tồn tại
BEGIN
    DBMS_SCHEDULER.DROP_JOB('JOB_GIAI_PHONG_GHE', FORCE => TRUE);
    DBMS_SCHEDULER.DROP_JOB('JOB_CAP_NHAT_TRANGTHAI_CHUYEN', FORCE => TRUE);
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

-- Drop views nếu đã tồn tại
BEGIN
    FOR v IN (
        SELECT view_name
        FROM user_views
        WHERE view_name IN (
            'V_DANH_SACH_CHUYEN',
            'V_LICH_SU_DAT_VE',
            'V_DOANH_THU_HANG_XE',
            'V_DANHGIA_HANG_XE'
        )
    ) LOOP
        EXECUTE IMMEDIATE 'DROP VIEW ' || v.view_name;
    END LOOP;
END;
/

-- Drop tables theo thứ tự có thể cascade
BEGIN
  FOR t IN (
    SELECT table_name
    FROM user_tables
    WHERE table_name IN (
      'HINHANH','TIENICHXE','TIENICH',
      'DANHGIA','THANHTOAN','HOADON',
      'VE','DATVE','DIEMDONTRA','DIEMBEN',
      'CHUYENXE','TUYENXE','GHE','XE',
      'LOAIXE','BENXE','NHAXE',
      'LOAIVE','KHUYENMAI',
      'NHANVIEN','KHACHHANG','TAIKHOAN'
    )
  ) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
  END LOOP;
END;
/

-- Drop sequences nếu đã tồn tại
-- Drop tables theo thứ tự có thể cascade
BEGIN
  FOR t IN (
    SELECT table_name
    FROM user_tables
    WHERE table_name IN (
      'HINHANH',
      'TIENICHXE',
      'TIENICH',

      'DANHGIA',
      'THANHTOAN',
      'HOADON',

      'VE',
      'DATVE',
      'DIEMDONTRA',
      'DIEMBEN',

      'CHUYENXE',
      'TUYENXE',
      'GHE',
      'XE',

      'LOAIXE',
      'BENXE',
      'NHAXE',

      'LOAIVE',
      'KHUYENMAI',

      'EMAIL_OTP',

      'NHANVIEN',
      'KHACHHANG',
      'TAIKHOAN'
    )
  ) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
  END LOOP;
END;
/

-- ============================================================

-- PHẦN 1: TẠO BẢNG (DDL)
-- Ghi chú: không dùng SYSDATE/SYSTIMESTAMP trong CHECK constraint
-- ============================================================

-- 1.1 TAIKHOAN
CREATE TABLE TAIKHOAN (
    MaTK          NUMBER          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    TenDangNhap  VARCHAR2(50)    NOT NULL,
    MatKhau      VARCHAR2(255)   NOT NULL,
    Quyen        VARCHAR2(20)    NOT NULL,
    TrangThaiTK  VARCHAR2(20)    DEFAULT 'Chưa xác minh' NOT NULL,
    NgayTao      TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT uq_tk_tendangnhap UNIQUE (TenDangNhap),
    CONSTRAINT chk_tk_quyen      CHECK (Quyen IN ('KhachHang','NhanVien','Admin')),
    CONSTRAINT chk_tk_trangthai  CHECK (TrangThaiTK IN ('Chưa xác minh','Hoạt động','Bị khóa'))
);

-- 1.2 KHACHHANG
CREATE TABLE KHACHHANG (
    MaKH        VARCHAR2(20)    PRIMARY KEY,
    TenKH       VARCHAR2(100)   NOT NULL,
    NgaySinh    DATE,
    GioiTinh    VARCHAR2(10),
    SDT         VARCHAR2(15),
    Email       VARCHAR2(100),
    MaTK        NUMBER          NOT NULL,
    TrangThai   VARCHAR2(20)    DEFAULT 'Chưa xác minh' NOT NULL,
    CONSTRAINT uq_kh_sdt        UNIQUE (SDT),
    CONSTRAINT uq_kh_email      UNIQUE (Email),
    CONSTRAINT uq_kh_matk       UNIQUE (MaTK),
    CONSTRAINT fk_kh_matk       FOREIGN KEY (MaTK) REFERENCES TAIKHOAN(MaTK),
    CONSTRAINT chk_kh_gioitinh  CHECK (GioiTinh IN ('Nam','Nữ','Khác')),
    CONSTRAINT chk_kh_trangthai CHECK (TrangThai IN ('Chưa xác minh','Hoạt động','Bị khóa'))
);

CREATE TABLE EMAIL_OTP (
    MaOTP        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    MaTK         NUMBER NOT NULL,
    Email        VARCHAR2(100) NOT NULL,
    OtpCode      VARCHAR2(10) NOT NULL,
    Purpose      VARCHAR2(30) NOT NULL,
    ExpiredAt    TIMESTAMP NOT NULL,
    Verified     NUMBER(1) DEFAULT 0 NOT NULL,
    AttemptCount NUMBER(2) DEFAULT 0 NOT NULL,
    CreatedAt    TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

    CONSTRAINT fk_email_otp_taikhoan
        FOREIGN KEY (MaTK) REFERENCES TAIKHOAN(MaTK),

    CONSTRAINT chk_email_otp_purpose
        CHECK (Purpose IN ('REGISTER', 'FORGOT_PASSWORD', 'CHANGE_EMAIL')),

    CONSTRAINT chk_email_otp_verified
        CHECK (Verified IN (0, 1)),

    CONSTRAINT chk_email_otp_attempt
        CHECK (AttemptCount >= 0)
);

CREATE INDEX idx_email_otp_email_purpose
ON EMAIL_OTP (Email, Purpose, Verified, CreatedAt);

CREATE INDEX idx_email_otp_matk_purpose
ON EMAIL_OTP (MaTK, Purpose, Verified, CreatedAt);

CREATE INDEX idx_email_otp_expired
ON EMAIL_OTP (ExpiredAt);

-- 1.3 NHAXE
CREATE TABLE NHAXE (
    MaNhaXe   VARCHAR2(20)    PRIMARY KEY,
    TenNhaXe  VARCHAR2(100)   NOT NULL,
    SDT       VARCHAR2(15),
    Email     VARCHAR2(100),
    DiaChi    VARCHAR2(200),
    TrangThai VARCHAR2(20),
    MoTa      CLOB,
    CONSTRAINT uq_nx_ten    UNIQUE (TenNhaXe),
    CONSTRAINT uq_nx_sdt    UNIQUE (SDT),
    CONSTRAINT uq_nx_email  UNIQUE (Email),
    CONSTRAINT chk_nhaxe_trangthai CHECK (TrangThai IN ('Hoạt động','Ngừng hoạt động'))
);


-- 1.4 NHANVIEN
CREATE TABLE NHANVIEN (
    MaNV        VARCHAR2(20)    PRIMARY KEY,
    TenNV       VARCHAR2(100)   NOT NULL,
    GioiTinh    VARCHAR2(10),
    SDT         VARCHAR2(15),
    Email       VARCHAR2(100),
    NgayVaoLam  DATE            NOT NULL,
    TrangThai   VARCHAR2(20)    DEFAULT 'Hoạt động' NOT NULL,
    MaNhaXe     VARCHAR2(20)    NOT NULL,
    MaTK        NUMBER          NOT NULL,
    CONSTRAINT uq_nv_sdt        UNIQUE (SDT),
    CONSTRAINT uq_nv_email      UNIQUE (Email),
    CONSTRAINT uq_nv_matk       UNIQUE (MaTK),
    CONSTRAINT fk_nv_nhaxe      FOREIGN KEY (MaNhaXe) REFERENCES NHAXE(MaNhaXe),
    CONSTRAINT fk_nv_matk       FOREIGN KEY (MaTK) REFERENCES TAIKHOAN(MaTK),
    CONSTRAINT chk_nv_gioitinh  CHECK (GioiTinh IN ('Nam','Nữ','Khác')),
    CONSTRAINT chk_nv_trangthai CHECK (TrangThai IN ('Hoạt động','Tạm ngưng','Nghỉ việc'))
);

-- 1.5 BENXE
CREATE TABLE BENXE (
    MaBen   VARCHAR2(20)    PRIMARY KEY,
    TenBen  VARCHAR2(100)   NOT NULL,
    DiaChi  VARCHAR2(200),
    CONSTRAINT uq_ben_ten UNIQUE (TenBen)
);

-- 1.6 DIEMBEN
-- Danh mục điểm đón/trả mẫu theo từng bến.
-- Khi tạo chuyến, nhân viên chọn từ DIEMBEN và backend copy sang DIEMDONTRA.
CREATE TABLE DIEMBEN (
    MaDiemBen  VARCHAR2(30)    PRIMARY KEY,
    MaBen      VARCHAR2(20)    NOT NULL,
    TenDiem    VARCHAR2(200)   NOT NULL,
    DiaChi     VARCHAR2(300),
    Loai       VARCHAR2(10)    NOT NULL,
    ThuTu      NUMBER(3)       NOT NULL,
    TrangThai  VARCHAR2(20)    DEFAULT 'Hoạt động' NOT NULL,
    CONSTRAINT fk_diemben_benxe       FOREIGN KEY (MaBen) REFERENCES BENXE(MaBen),
    CONSTRAINT chk_diemben_loai       CHECK (Loai IN ('Đón','Trả','Cả hai')),
    CONSTRAINT chk_diemben_thutu      CHECK (ThuTu > 0),
    CONSTRAINT chk_diemben_trangthai  CHECK (TrangThai IN ('Hoạt động','Tạm ngưng')),
    CONSTRAINT uq_diemben_ben_loai_thutu UNIQUE (MaBen, Loai, ThuTu)
);

-- 1.7 LOAIXE
CREATE TABLE LOAIXE (
    MaLoaiXe   VARCHAR2(20)    PRIMARY KEY,
    TenLoaiXe  VARCHAR2(50)    NOT NULL,
    MoTa       VARCHAR2(500),
    CONSTRAINT uq_loaixe_ten UNIQUE (TenLoaiXe)
);

-- 1.8 XE
CREATE TABLE XE (
    MaXe        VARCHAR2(20)    PRIMARY KEY,
    MaNhaXe     VARCHAR2(20)    NOT NULL,
    BienSo      VARCHAR2(20)    NOT NULL,
    MaLoaiXe    VARCHAR2(20)    NOT NULL,
    SoLuongGhe  NUMBER(3)       NOT NULL,
    TrangThai   VARCHAR2(30)    DEFAULT 'Hoạt động' NOT NULL,
    CONSTRAINT uq_xe_bienso     UNIQUE (BienSo),
    CONSTRAINT fk_xe_nhaxe      FOREIGN KEY (MaNhaXe) REFERENCES NHAXE(MaNhaXe),
    CONSTRAINT fk_xe_loaixe     FOREIGN KEY (MaLoaiXe) REFERENCES LOAIXE(MaLoaiXe),
    CONSTRAINT chk_xe_soghe     CHECK (SoLuongGhe > 0),
    CONSTRAINT chk_xe_trangthai CHECK (TrangThai IN ('Hoạt động','Bảo dưỡng','Ngừng hoạt động'))
);

-- 1.9 GHE
CREATE TABLE GHE (
    MaGhe  VARCHAR2(20)    PRIMARY KEY,
    MaXe   VARCHAR2(20)    NOT NULL,
    SoGhe  VARCHAR2(10)    NOT NULL,
    CONSTRAINT uq_ghe_xe_so UNIQUE (MaXe, SoGhe),
    CONSTRAINT fk_ghe_xe    FOREIGN KEY (MaXe) REFERENCES XE(MaXe)
);

-- 1.10 TUYENXE
CREATE TABLE TUYENXE (
    MaTuyen         VARCHAR2(20)    PRIMARY KEY,
    MaBenDi         VARCHAR2(20)    NOT NULL,
    MaBenDen        VARCHAR2(20)    NOT NULL,
    KhoangCach      NUMBER(6)       NOT NULL,
    ThoiGianDuKien  NUMBER(5)       NOT NULL,
    CONSTRAINT uq_tuyen_ben         UNIQUE (MaBenDi, MaBenDen),
    CONSTRAINT fk_tuyen_bendi       FOREIGN KEY (MaBenDi)  REFERENCES BENXE(MaBen),
    CONSTRAINT fk_tuyen_benden      FOREIGN KEY (MaBenDen) REFERENCES BENXE(MaBen),
    CONSTRAINT chk_tuyen_khacben    CHECK (MaBenDi <> MaBenDen),
    CONSTRAINT chk_tuyen_khoangcach CHECK (KhoangCach > 0),
    CONSTRAINT chk_tuyen_thoigian   CHECK (ThoiGianDuKien > 0)
);

-- 1.11 CHUYENXE
CREATE TABLE CHUYENXE (
    MaChuyen          VARCHAR2(20)   PRIMARY KEY,
    MaXe              VARCHAR2(20)   NOT NULL,
    MaTuyen           VARCHAR2(20)   NOT NULL,
    ThoiGianKhoiHanh  TIMESTAMP      NOT NULL,
    ThoiGianDen       TIMESTAMP      NOT NULL,
    GiaVe             NUMBER(12)     NOT NULL,
    TrangThai         VARCHAR2(20)   DEFAULT 'Sắp chạy' NOT NULL,
    CONSTRAINT uq_chuyen_xe_gio     UNIQUE (MaXe, ThoiGianKhoiHanh),
    CONSTRAINT fk_chuyen_xe         FOREIGN KEY (MaXe)    REFERENCES XE(MaXe),
    CONSTRAINT fk_chuyen_tuyen      FOREIGN KEY (MaTuyen) REFERENCES TUYENXE(MaTuyen),
    CONSTRAINT chk_chuyen_thoigian  CHECK (ThoiGianDen > ThoiGianKhoiHanh),
    CONSTRAINT chk_chuyen_gia       CHECK (GiaVe > 0),
    CONSTRAINT chk_chuyen_trangthai CHECK (TrangThai IN ('Sắp chạy','Đang mở bán','Đang chạy','Hoàn thành','Đã hủy'))
);

-- 1.12 DIEMDONTRA
-- Điểm đón/trả áp dụng cho từng chuyến.
-- MaDiemBen là điểm mẫu theo bến, có thể NULL để cho phép điểm ngoài danh mục.
CREATE TABLE DIEMDONTRA (
    MaDiem     VARCHAR2(20)    PRIMARY KEY,
    MaChuyen   VARCHAR2(20)    NOT NULL,
    MaDiemBen  VARCHAR2(30),
    MaBen      VARCHAR2(20)    NOT NULL,
    TenDiem    VARCHAR2(200)   NOT NULL,
    ThoiGian   TIMESTAMP,
    Loai       VARCHAR2(10)    NOT NULL,
    ThuTu      NUMBER(3)       NOT NULL,
    CONSTRAINT fk_ddt_chuyen       FOREIGN KEY (MaChuyen)  REFERENCES CHUYENXE(MaChuyen),
    CONSTRAINT fk_ddt_diemben      FOREIGN KEY (MaDiemBen) REFERENCES DIEMBEN(MaDiemBen),
    CONSTRAINT fk_ddt_benxe        FOREIGN KEY (MaBen)     REFERENCES BENXE(MaBen),
    CONSTRAINT chk_ddt_loai        CHECK (Loai IN ('Đón','Trả')),
    CONSTRAINT chk_ddt_thutu       CHECK (ThuTu > 0),
    CONSTRAINT uq_ddt_chuyen_loai_thutu UNIQUE (MaChuyen, Loai, ThuTu)
);

-- 1.13 LOAIVE
CREATE TABLE LOAIVE (
    MaLoaiVe   VARCHAR2(20)    PRIMARY KEY,
    TenLoaiVe  NVARCHAR2(100)  NOT NULL,
    HeSoGia    NUMBER(5,2)     DEFAULT 1 NOT NULL,
    MoTa       NVARCHAR2(300),
    TrangThai  NVARCHAR2(20)   DEFAULT N'Hoạt động' NOT NULL,

    CONSTRAINT uq_loaive_ten
        UNIQUE (TenLoaiVe),

    CONSTRAINT ck_loaive_hesogia
        CHECK (HeSoGia > 0),

    CONSTRAINT ck_loaive_trangthai
        CHECK (TrangThai IN (N'Hoạt động', N'Tạm ngưng'))
);

-- 1.14 KHUYENMAI
CREATE TABLE KHUYENMAI (
    MaKhuyenMai   VARCHAR2(20)    PRIMARY KEY,
    TenKhuyenMai  VARCHAR2(100)   NOT NULL,
    PhanTramGiam  NUMBER(5,2)     DEFAULT 0 NOT NULL,
    SoTienGiam    NUMBER(12)      DEFAULT 0 NOT NULL,
    NgayBatDau    TIMESTAMP       NOT NULL,
    NgayKetThuc   TIMESTAMP       NOT NULL,
    TrangThai     VARCHAR2(20)    DEFAULT 'Đang áp dụng' NOT NULL,
    CONSTRAINT chk_km_phantram  CHECK (PhanTramGiam BETWEEN 0 AND 100),
    CONSTRAINT chk_km_sotien    CHECK (SoTienGiam >= 0),
    CONSTRAINT chk_km_ngay      CHECK (NgayKetThuc > NgayBatDau),
    CONSTRAINT chk_km_exclusive CHECK (
        (PhanTramGiam > 0 AND SoTienGiam = 0)
        OR (PhanTramGiam = 0 AND SoTienGiam > 0)
    ),
    CONSTRAINT chk_km_trangthai CHECK (TrangThai IN ('Đang áp dụng','Hết hạn','Tạm dừng'))
);

-- 1.15 DATVE
CREATE TABLE DATVE (
    MaDatVe    VARCHAR2(30)    PRIMARY KEY,
    MaKH       VARCHAR2(20)    NOT NULL,
    NgayDat    TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    TrangThai  VARCHAR2(30)    DEFAULT 'Chờ thanh toán' NOT NULL,
    CONSTRAINT fk_datve_kh   FOREIGN KEY (MaKH) REFERENCES KHACHHANG(MaKH),
    CONSTRAINT chk_datve_tt  CHECK (TrangThai IN ('Chờ thanh toán','Đã thanh toán','Đã hủy','Hết hạn'))
);

-- 1.16 VE
-- Không đặt UNIQUE(MaChuyen, MaGhe) dạng constraint thường,
-- vì vé đã hủy phải được giải phóng ghế.
-- Sẽ dùng function-based unique index ở phần INDEX.
CREATE TABLE VE (
    MaVe            VARCHAR2(20) PRIMARY KEY,
    MaDatVe         VARCHAR2(20),
    MaChuyen        VARCHAR2(20) NOT NULL,
    MaGhe           VARCHAR2(20) NOT NULL,
    MaKH            VARCHAR2(20),
    MaLoaiVe        VARCHAR2(20) NOT NULL,
    MaDiemDon       VARCHAR2(20),
    MaDiemTra       VARCHAR2(20),
    GiaTien         NUMBER(12, 2) NOT NULL,
    TrangThai       NVARCHAR2(50) DEFAULT N'Đã đặt' NOT NULL,
    ThoiGianDat     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ThoiGianGiuDen  TIMESTAMP,

    CONSTRAINT FK_VE_DATVE
        FOREIGN KEY (MaDatVe)
        REFERENCES DATVE(MaDatVe),

    CONSTRAINT FK_VE_CHUYENXE
        FOREIGN KEY (MaChuyen)
        REFERENCES CHUYENXE(MaChuyen),

    CONSTRAINT FK_VE_GHE
        FOREIGN KEY (MaGhe)
        REFERENCES GHE(MaGhe),

    CONSTRAINT FK_VE_KHACHHANG
        FOREIGN KEY (MaKH)
        REFERENCES KHACHHANG(MaKH),

    CONSTRAINT FK_VE_LOAIVE
        FOREIGN KEY (MaLoaiVe)
        REFERENCES LOAIVE(MaLoaiVe),

    CONSTRAINT FK_VE_DIEMDON
        FOREIGN KEY (MaDiemDon)
        REFERENCES DIEMDONTRA(MaDiem),

    CONSTRAINT FK_VE_DIEMTRA
        FOREIGN KEY (MaDiemTra)
        REFERENCES DIEMDONTRA(MaDiem),

    CONSTRAINT CK_VE_GIATIEN
        CHECK (GiaTien >= 0),

    CONSTRAINT CK_VE_TRANGTHAI
        CHECK (TrangThai IN (
            N'Giữ chỗ',
            N'Đã đặt',
            N'Đã thanh toán',
            N'Đã hủy',
            N'Đã dùng'
        ))
);

-- 1.17 HOADON
CREATE TABLE HOADON (
    MaHoaDon    VARCHAR2(30)    PRIMARY KEY,
    MaDatVe     VARCHAR2(30)    NOT NULL,
    NgayLap     TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    GiaGoc      NUMBER(12)      DEFAULT 0 NOT NULL,
    TienGiam    NUMBER(12)      DEFAULT 0 NOT NULL,
    TongTien    NUMBER(12)      DEFAULT 0 NOT NULL,
    MaKhuyenMai VARCHAR2(20),
    TrangThai   VARCHAR2(30)    DEFAULT 'Chưa thanh toán' NOT NULL,
    CONSTRAINT uq_hd_datve      UNIQUE (MaDatVe),
    CONSTRAINT fk_hd_datve      FOREIGN KEY (MaDatVe)     REFERENCES DATVE(MaDatVe),
    CONSTRAINT fk_hd_khuyenmai  FOREIGN KEY (MaKhuyenMai) REFERENCES KHUYENMAI(MaKhuyenMai),
    CONSTRAINT chk_hd_tien      CHECK (GiaGoc >= 0 AND TienGiam >= 0 AND TongTien >= 0),
    CONSTRAINT chk_hd_trangthai CHECK (TrangThai IN ('Chưa thanh toán','Đã thanh toán','Đã hủy'))
);

-- 1.18 THANHTOAN
-- LoaiGiaoDich giúp phân biệt thanh toán và hoàn tiền.
-- SoTien luôn dương; hoàn tiền được ghi LoaiGiaoDich = 'HoanTien'.
CREATE TABLE THANHTOAN (
    MaThanhToan         VARCHAR2(30) PRIMARY KEY,
    MaHoaDon            VARCHAR2(30) NOT NULL,

    OrderCode           NUMBER NOT NULL UNIQUE,
    PayOSPaymentLinkId  VARCHAR2(100),
    CheckoutUrl         VARCHAR2(1000),

    LoaiGiaoDich        VARCHAR2(20) DEFAULT 'ThanhToan' NOT NULL,
    PhuongThucThanhToan VARCHAR2(30) DEFAULT 'PAYOS' NOT NULL,

    SoTien              NUMBER(12,2) NOT NULL,
    TrangThai           VARCHAR2(30) DEFAULT 'Đang xử lý' NOT NULL,

    NgayTao             TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    NgayThanhToan       TIMESTAMP,
    NgayCapNhat         TIMESTAMP,

    CONSTRAINT fk_tt_hoadon 
        FOREIGN KEY (MaHoaDon)
        REFERENCES HOADON(MaHoaDon),

    CONSTRAINT chk_tt_loaigd 
        CHECK (LoaiGiaoDich IN ('ThanhToan','HoanTien')),

    CONSTRAINT chk_tt_phuongthuc 
        CHECK (PhuongThucThanhToan IN ('PAYOS','TIEN_MAT','CHUYEN_KHOAN')),

    CONSTRAINT chk_tt_sotien 
        CHECK (SoTien > 0),

    CONSTRAINT chk_tt_trangthai 
        CHECK (TrangThai IN ('Thành công','Không thành công','Đang xử lý','Đã hủy'))
);

-- 1.19 DANHGIA
CREATE TABLE DANHGIA (
    MaDanhGia   VARCHAR2(20)    PRIMARY KEY,
    MaKH        VARCHAR2(20)    NOT NULL,
    MaChuyen    VARCHAR2(20)    NOT NULL,
    SoSao       NUMBER(1)       NOT NULL,
    NoiDung     CLOB,
    NgayDanhGia TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT uq_dg_kh_chuyen  UNIQUE (MaKH, MaChuyen),
    CONSTRAINT fk_dg_kh         FOREIGN KEY (MaKH)     REFERENCES KHACHHANG(MaKH),
    CONSTRAINT fk_dg_chuyen     FOREIGN KEY (MaChuyen) REFERENCES CHUYENXE(MaChuyen),
    CONSTRAINT chk_dg_sosao     CHECK (SoSao BETWEEN 1 AND 5)
);

-- 1.20 TIENICH
CREATE TABLE TIENICH (
    MaTienIch   VARCHAR2(20)    PRIMARY KEY,
    TenTienIch  VARCHAR2(100)   NOT NULL,
    CONSTRAINT uq_ti_ten UNIQUE (TenTienIch)
);

-- 1.21 TIENICHXE
CREATE TABLE TIENICHXE (
    MaXe       VARCHAR2(20)    NOT NULL,
    MaTienIch  VARCHAR2(20)    NOT NULL,
    CONSTRAINT pk_tienichxe    PRIMARY KEY (MaXe, MaTienIch),
    CONSTRAINT fk_tix_xe       FOREIGN KEY (MaXe)      REFERENCES XE(MaXe),
    CONSTRAINT fk_tix_tienich  FOREIGN KEY (MaTienIch) REFERENCES TIENICH(MaTienIch)
);

-- 1.22 HINHANH
CREATE TABLE HINHANH (
    MaAnh  VARCHAR2(20)    PRIMARY KEY,
    MaXe   VARCHAR2(20)    NOT NULL,
    URL    VARCHAR2(500)   NOT NULL,
    ThuTu  NUMBER(3)       DEFAULT 1 NOT NULL,
    CONSTRAINT fk_hh_xe     FOREIGN KEY (MaXe) REFERENCES XE(MaXe),
    CONSTRAINT chk_hh_thutu CHECK (ThuTu > 0)
);

-- ============================================================
