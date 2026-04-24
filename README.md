# 🚌 Hệ Thống Bán Vé Xe Bus Trực Tuyến

> **Nền tảng trung gian kết nối hành khách với các hãng xe khách, được xây dựng trên Oracle Database**

---

## 1. Giới thiệu

**Hệ Thống Bán Vé Xe Bus Trực Tuyến** là một nền tảng trung gian (marketplace) cho phép nhiều hãng xe khách đăng ký, quản lý lịch trình và vận hành bán vé trên cùng một cổng thông tin thống nhất. Hành khách có thể tìm kiếm chuyến xe, chọn ghế, đặt vé và thanh toán trực tuyến một cách thuận tiện.

Hệ thống được thiết kế theo mô hình **Data Sync (Mô hình 2)**, nghĩa là các hãng xe chủ động nhập và cập nhật dữ liệu lịch trình trực tiếp trên hệ thống, không cần tích hợp API phức tạp. Cách tiếp cận này đặc biệt phù hợp với các hãng xe vừa và nhỏ tại Việt Nam.

---

## 2. Mục tiêu hệ thống

Hệ thống được xây dựng nhằm các mục tiêu sau:

- Số hóa quy trình tìm kiếm, đặt vé và thanh toán vé xe khách
- Tạo nền tảng quản lý tập trung cho nhiều hãng xe trên cùng một hệ thống
- Hỗ trợ quản lý lịch trình, đội xe, điểm đón trả và doanh thu hiệu quả
- Nâng cao trải nghiệm khách hàng thông qua vé điện tử và QR Code
- Tăng khả năng mở rộng và đồng bộ dữ liệu trong môi trường nhiều đối tác

---

## 3. Tính năng chính

### 3.1. Dành cho khách hàng

- Tìm kiếm chuyến xe theo điểm đi, điểm đến và ngày khởi hành
- Xem sơ đồ ghế và lựa chọn vị trí ghế mong muốn
- Đặt vé trực tuyến nhanh chóng
- Thanh toán qua nhiều phương thức như **VNPay**, **MoMo** hoặc **tiền mặt**
- Nhận vé điện tử dưới dạng **QR Code** qua **Email** hoặc **SMS**
- Hủy vé và nhận hoàn tiền tự động theo chính sách
- Gửi đánh giá sau khi hoàn thành chuyến đi

### 3.2. Dành cho hãng xe

- Quản lý đội xe và sơ đồ ghế của từng xe
- Tạo và quản lý tuyến đường, chuyến xe, lịch trình vận hành
- Thiết lập điểm đón và điểm trả linh hoạt dọc tuyến
- Xem báo cáo doanh thu theo ngày, tuyến hoặc xe
- Check-in hành khách bằng mã QR
- Tải lên hình ảnh xe để hiển thị trên nền tảng

### 3.3. Dành cho quản trị viên

- Quản lý toàn bộ hãng xe đối tác trên hệ thống
- Thiết lập chương trình khuyến mãi theo phần trăm hoặc số tiền cố định
- Đối soát doanh thu và tính hoa hồng định kỳ
- Theo dõi dashboard tổng quan doanh thu toàn hệ thống

---

## 4. Kiến trúc cơ sở dữ liệu

Hệ thống sử dụng **Oracle Database** với **21 bảng**, được tổ chức thành **5 nhóm chức năng chính** như sau:

```text
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Quản lý tài   │    │  Quản lý hãng   │    │  Quản lý tuyến  │
│     khoản       │    │   xe & đội xe   │    │  & lịch trình   │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ TAIKHOAN        │    │ NHAXE           │    │ TUYENXE         │
│ KHACHHANG       │    │ XE              │    │ CHUYENXE        │
│ NHANVIEN        │    │ GHE             │    │ DIEMDONTRA      │
│                 │    │ LOAIXE          │    │                 │
│                 │    │ TIENICH         │    │                 │
│                 │    │ TIENICHXE       │    │                 │
│                 │    │ HINHANH         │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘

┌─────────────────┐    ┌─────────────────┐
│   Đặt vé & Vé   │    │ Thanh toán &    │
│                 │    │   Khuyến mãi    │
├─────────────────┤    ├─────────────────┤
│ DATVE           │    │ HOADON          │
│ VE              │    │ THANHTOAN       │
│ LOAIVE          │    │ KHUYENMAI       │
│ BENXE           │    │ DANHGIA         │
└─────────────────┘    └─────────────────┘
```

---

## 5. Danh sách nhóm bảng

### 5.1. Nhóm quản lý tài khoản
- `TAIKHOAN`
- `KHACHHANG`
- `NHANVIEN`

### 5.2. Nhóm quản lý hãng xe và đội xe
- `NHAXE`
- `XE`
- `GHE`
- `LOAIXE`
- `TIENICH`
- `TIENICHXE`
- `HINHANH`

### 5.3. Nhóm quản lý tuyến đường và lịch trình
- `TUYENXE`
- `CHUYENXE`
- `DIEMDONTRA`

### 5.4. Nhóm đặt vé và vé xe
- `DATVE`
- `VE`
- `LOAIVE`
- `BENXE`

### 5.5. Nhóm thanh toán, khuyến mãi và đánh giá
- `HOADON`
- `THANHTOAN`
- `KHUYENMAI`
- `DANHGIA`

---

## 6. Trigger

Hệ thống sử dụng **9 trigger** để kiểm soát dữ liệu và tự động hóa một số nghiệp vụ quan trọng.

| STT | Tên Trigger | Bảng | Mục đích |
|-----|-------------|------|----------|
| 1 | `TRG_KiemTraLichXe` | `CHUYENXE` | Ngăn một xe chạy hai chuyến trùng thời gian |
| 2 | `TRG_KiemTraGheXe` | `VE` | Kiểm tra ghế phải thuộc đúng xe của chuyến |
| 3 | `TRG_KiemTraDiemDonTra` | `VE` | Kiểm tra điểm đón và điểm trả hợp lệ, đúng thứ tự tuyến |
| 4 | `TRG_SetVeGiuCho` | `VE` | Tự động đặt trạng thái giữ chỗ và timeout 15 phút |
| 5 | `TRG_CapNhatHoaDon` | `HOADON` | Tự động tính tổng tiền = giá gốc − tiền giảm |
| 6 | `TRG_CapNhatDonSauThanhToan` | `THANHTOAN` | Đồng bộ trạng thái từ thanh toán sang hóa đơn, đơn đặt và vé |
| 7 | `TRG_HuyVeKhiDatVeHuy` | `DATVE` | Tự động hủy vé khi đơn đặt vé bị hủy |
| 8 | `TRG_KiemTraDanhGia` | `DANHGIA` | Chỉ cho phép đánh giá khi chuyến xe đã hoàn thành |
| 9 | `TRG_KhoaTKKhiNVNghiViec` | `NHANVIEN` | Tự động khóa tài khoản khi nhân viên nghỉ việc |

---

## 7. Stored Procedure

Hệ thống triển khai **6 stored procedure** nhằm gom nhóm nghiệp vụ quan trọng theo từng transaction.

| STT | Tên Procedure | Mô tả |
|-----|---------------|-------|
| 1 | `SP_TinhTienGiam` | Tính tiền giảm theo khuyến mãi dạng phần trăm hoặc số tiền cố định |
| 2 | `SP_DatVe` | Thực hiện quy trình đặt vé: kiểm tra, tạo đơn, tạo vé, tạo hóa đơn trong cùng một transaction |
| 3 | `SP_ThanhToan` | Ghi nhận thanh toán, kiểm tra số tiền và kích hoạt cập nhật trạng thái liên quan |
| 4 | `SP_HuyDatVe` | Hủy đặt vé và tính mức hoàn tiền theo thời gian trước giờ khởi hành |
| 5 | `SP_GiaiPhongGheHetHan` | Giải phóng ghế quá thời gian giữ chỗ, dự kiến chạy định kỳ mỗi 2 phút |
| 6 | `SP_KiemTraVeQR` | Kiểm tra vé QR khi check-in và xác thực quyền của hãng xe |

---

## 8. Ràng buộc toàn vẹn

Hệ thống có **92 ràng buộc toàn vẹn**, được chia thành 5 nhóm chính:

| Nhóm ràng buộc | Số lượng | Mô tả |
|----------------|----------|-------|
| Khóa chính | 21 | Mỗi bảng có khóa chính duy nhất và không null |
| Khóa ngoại | 26 | Các quan hệ tham chiếu được kiểm soát bằng khóa ngoại với quy tắc `RESTRICT` |
| Miền giá trị | 32 | Bao gồm `CHECK`, `UNIQUE`, giới hạn số, ngày giờ và trạng thái |
| Liên thuộc tính | 9 | Ràng buộc giữa các cột trong cùng bảng hoặc giữa các bảng liên quan |
| Liên bộ/liên quan hệ | 4 | Các ràng buộc nghiệp vụ phức tạp nhiều bảng |

---

## 9. Cấu trúc thư mục 

```text
ONLINE-BUS-TICKET-SYSTEM/
├── .github/                  # Cấu hình GitHub, workflow hoặc issue template nếu có
├── .idea/                    # Cấu hình dự án của IntelliJ IDEA
├── .vscode/                  # Cấu hình dự án của Visual Studio Code
├── backend/                  # Mã nguồn backend
│   ├── bus-ticket-api/       # Thư mục chứa project Spring Boot API
│   │   └── bus-ticket-api/   # Source code chính của backend
│   └── readme.md             # Tài liệu mô tả backend
├── database/                 # Các script cơ sở dữ liệu
│   ├── 01_create_tables.sql        # Tạo các bảng dữ liệu
│   ├── 02_constraints.sql          # Tạo ràng buộc khóa chính, khóa ngoại, check
│   ├── 03_insert_sample_data.sql   # Thêm dữ liệu mẫu
│   ├── 04_triggers.sql             # Tạo trigger xử lý tự động
│   ├── 05_procedures.sql           # Tạo stored procedure
│   ├── 06_views.sql                # Tạo view phục vụ truy vấn
│   └── 07_jobs.sql                 # Tạo job/scheduler tự động
├── frontend/                 # Mã nguồn frontend
│   ├── bus-ticket-web/       # Giao diện website bán vé xe
│   └── readme.md             # Tài liệu mô tả frontend
└── README.md                 # Tài liệu tổng quan của toàn bộ dự án
```

Cấu trúc này giúp tách riêng tài liệu, mã nguồn và các script cơ sở dữ liệu để dễ quản lý, triển khai và bảo trì.

---

## 10. Hướng dẫn cài đặt

### 10.1. Yêu cầu môi trường

- **Oracle Database 19c** trở lên hoặc **Oracle XE 21c**
- **SQL*Plus** hoặc **Oracle SQL Developer**
- Tài khoản có các quyền:
  - `CREATE TABLE`
  - `CREATE PROCEDURE`
  - `CREATE TRIGGER`
  - `CREATE SEQUENCE`
  - `CREATE VIEW`
  - `CREATE JOB`

### 10.2. Các bước cài đặt

Thực hiện lần lượt các script theo thứ tự sau:

```sql
-- 1. Tạo bảng
@01_create_tables.sql

-- 2. Tạo ràng buộc
@02_constraints.sql

-- 3. Chèn dữ liệu mẫu
@03_insert_sample_data.sql

-- 4. Tạo trigger
@04_triggers.sql

-- 5. Tạo stored procedure
@05_procedures.sql

-- 6. Tạo view
@06_views.sql

-- 7. Tạo scheduler job
@07_jobs.sql
```

Nếu sử dụng **SQL Developer**, có thể mở từng file và chạy tuần tự bằng chức năng **Run Script**.

---

## 11. Kiểm tra sau cài đặt

Sau khi cài đặt, có thể kiểm tra hệ thống bằng các bước sau:

- Kiểm tra số lượng bảng đã tạo
- Kiểm tra trigger và procedure đã biên dịch thành công
- Chạy thử chức năng đặt vé bằng `SP_DatVe`
- Chạy thử thanh toán bằng `SP_ThanhToan`
- Kiểm tra job giải phóng ghế giữ chỗ nếu đã cấu hình scheduler

Ví dụ:

```sql
SELECT table_name FROM user_tables;
SELECT object_name, status FROM user_objects WHERE object_type IN ('TRIGGER', 'PROCEDURE');
```

---

## 12. Ví dụ sử dụng

### 12.1. Đặt vé

Người dùng tìm chuyến xe phù hợp, chọn ghế trống và tiến hành đặt vé. Hệ thống sẽ:

1. Kiểm tra ghế còn trống hay không
2. Tạo bản ghi đặt vé
3. Tạo vé tương ứng
4. Tạo hóa đơn
5. Đưa vé vào trạng thái giữ chỗ trong 15 phút nếu chưa thanh toán

### 12.2. Thanh toán

Khi khách hàng thanh toán thành công:

1. Hệ thống ghi nhận giao dịch vào bảng `THANHTOAN`
2. Trigger cập nhật trạng thái hóa đơn
3. Trạng thái đơn đặt vé và vé được cập nhật tương ứng

### 12.3. Hủy vé

Khi khách hàng hủy vé:

- Hệ thống xác định thời gian còn lại trước giờ khởi hành
- Áp dụng chính sách hoàn tiền tương ứng: **100%**, **50%** hoặc **0%**
- Cập nhật trạng thái đơn và vé liên quan

---

## 13. Công nghệ sử dụng

### Kiến trúc công nghệ

- **Database:** Oracle Database 19c/21c (Triggers, Stored Procedures, Scheduler Jobs)
- **Backend:** Java 17, Spring Boot 3.x, Spring Data JPA
- **Frontend:** HTML5, CSS3, JavaScript, Bootstrap 5
- **Security:** JWT (JSON Web Token), BCrypt Password Hashing


## 14. Lưu ý quan trọng

> **Bảo mật:** Trong dữ liệu mẫu, mật khẩu đang được biểu diễn dưới dạng chuỗi như `'hashed_...'`. Trong môi trường thực tế, cần hash mật khẩu bằng `DBMS_CRYPTO` hoặc xử lý hash ở tầng ứng dụng trước khi lưu vào cơ sở dữ liệu.

> **Scheduler Job:** Job `JOB_GIAI_PHONG_GHE` yêu cầu quyền `CREATE JOB`. Nếu tài khoản không có quyền này, có thể chạy thủ công procedure `SP_GiaiPhongGheHetHan` hoặc nhờ DBA hỗ trợ tạo job.

> **Dữ liệu mẫu:** Các mốc thời gian mẫu như **tháng 07/2025** có thể cần điều chỉnh để phù hợp với thời điểm kiểm thử thực tế.

---

## 15. Hướng phát triển

Trong tương lai, hệ thống có thể mở rộng thêm các chức năng sau:

- Tích hợp API với các hãng xe lớn để đồng bộ dữ liệu tự động
- Gợi ý chuyến xe phù hợp bằng thuật toán đề xuất
- Ứng dụng di động cho khách hàng và nhân viên nhà xe
- Dashboard phân tích dữ liệu doanh thu và hành vi khách hàng theo thời gian thực
- Tích hợp chatbot hỗ trợ đặt vé tự động

---

## 16. Tác giả

- Đồ án môn học
- Đề tài: **Hệ Thống Bán Vé Xe Bus Trực Tuyến**
- Công nghệ: **Oracle Database · HTML · CSS · JavaScript · Java Spring Boot**
