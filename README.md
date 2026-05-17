# 🚌 Hệ Thống Bán Vé Xe Khách Trực Tuyến

> **Nền tảng trung gian kết nối hành khách với các hãng xe khách, được xây dựng trên Oracle Database**

---

## Quick Start — Chạy nhanh (Windows / Linux)

1) Clone project:

   git clone <REPO_URL>
   cd Online-Bus-Ticket-System

2) Chuẩn bị cơ sở dữ liệu (Oracle XE/19c+)

   - Cài Oracle XE hoặc Oracle 19c và đảm bảo listener đang chạy (thường trên `localhost:1521`, service name `XE`).
   - Dùng `SQL*Plus` hoặc `SQL Developer` để chạy tuần tự các script trong thư mục `database/`:

     -- Kết nối ví dụ (SQL*Plus):
     sqlplus system/<YOUR_PASSWORD>@//localhost:1521/XE

     -- Tạo 1 kết nối mới trong oracle sau đó chạy lần lượt:

     @database/01_create_tables.sql
     @database/02_constraints.sql
     @database/03_insert_sample_data.sql
     @database/04_triggers.sql
     @database/05_procedures.sql
     @database/06_views.sql
     @database/07_jobs.sql

   - Lưu ý: Tốt nhất tạo một user riêng (không dùng `SYSTEM`) và cập nhật `spring.datasource.*` trong file cấu hình của backend.

3) Cấu hình backend

   - Mở [backend/bus-ticket-api/bus-ticket-api/src/main/resources/application.properties](backend/bus-ticket-api/bus-ticket-api/src/main/resources/application.properties#L1) (nếu không có, tạo file) và kiểm tra/cập nhật các thông số chính:
     - `server.port` — Cổng chạy ứng dụng (mặc định `8080`).
     - `spring.datasource.url` — JDBC URL tới Oracle (ví dụ `jdbc:oracle:thin:@//localhost:1521/XE`).
     - `spring.datasource.username` — database user.
     - `spring.datasource.password` — mật khẩu database.
     - `spring.datasource.driver-class-name` — driver JDBC (ví dụ `oracle.jdbc.OracleDriver`).
     - `app.frontend-url` — URL frontend (mặc định `http://127.0.0.1:5501/frontend/bus-ticket-web`).
     - Các secret/khóa: `payos.client-id`, `payos.api-key`, `payos.checksum-key`, `app.jwt.secret`, `spring.mail.username`, `spring.mail.password`.

   - Khuyến nghị: không commit secrets vào repository. Tạo file mẫu `application.properties.example` và copy thành `application.properties` trước khi chạy.

     - Windows (PowerShell):

       copy backend\bus-ticket-api\bus-ticket-api\src\main\resources\application.properties.example backend\bus-ticket-api\bus-ticket-api\src\main\resources\application.properties

     - Linux / macOS:

       cp backend/bus-ticket-api/bus-ticket-api/src/main/resources/application.properties.example backend/bus-ticket-api/bus-ticket-api/src/main/resources/application.properties

   - Thay thế secrets bằng biến môi trường (Spring Boot sẽ đọc các property từ env vars). Ví dụ:

     - PowerShell:

       $env:SPRING_DATASOURCE_URL="jdbc:oracle:thin:@//localhost:1521/XE"
       $env:SPRING_DATASOURCE_USERNAME="myuser"
       $env:SPRING_DATASOURCE_PASSWORD="mypassword"

     - Linux / macOS:

       export SPRING_DATASOURCE_URL="jdbc:oracle:thin:@//localhost:1521/XE"
       export SPRING_DATASOURCE_USERNAME="myuser"
       export SPRING_DATASOURCE_PASSWORD="mypassword"

   - Ví dụ nội dung `application.properties.example` (đặt trong `backend/bus-ticket-api/bus-ticket-api/src/main/resources/`):

      ```properties
      spring.application.name=bus-ticket-api
      server.port=8080

      spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/XE
      spring.datasource.username=DB_USER
      spring.datasource.password=DB_PASS
      spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

      spring.jpa.hibernate.ddl-auto=validate
      spring.jpa.show-sql=true

      payos.client-id=${PAYOS_CLIENT_ID:}
      payos.api-key=${PAYOS_API_KEY:}
      payos.checksum-key=${PAYOS_CHECKSUM_KEY:}

      app.frontend-url=${FRONTEND_URL:http://127.0.0.1:5501/frontend/bus-ticket-web}
      app.jwt.secret=${JWT_SECRET:change-me}
      app.jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}

      spring.mail.host=smtp.gmail.com
      spring.mail.port=587
      spring.mail.properties.mail.smtp.auth=true
      spring.mail.properties.mail.smtp.starttls.enable=true
      spring.mail.username=${MAIL_USERNAME:}
      spring.mail.password=${MAIL_PASSWORD:}
```

   - Ghi chú:
     - Nếu project đang dùng `application.properties` trong `target/classes` (build artifact), chỉnh file nguồn trong `src/main/resources` và rebuild.
     - Thay `DB_USER` / `DB_PASS` bằng user riêng (không dùng `SYSTEM`).
     - Kiểm tra các giá trị `payos.*` và `spring.mail.*` — thay bằng biến môi trường khi đưa lên môi trường production.

4) Build & chạy backend (sử dụng Maven Wrapper)

   - Windows:

     backend\bus-ticket-api\bus-ticket-api> mvnw.cmd clean package
     backend\bus-ticket-api\bus-ticket-api> mvnw.cmd spring-boot:run

   - Linux / macOS:

     cd backend/bus-ticket-api/bus-ticket-api
     ./mvnw clean package
     ./mvnw spring-boot:run

   - Hoặc chạy trực tiếp JAR sau khi `package`:

     java -jar backend/bus-ticket-api/bus-ticket-api/target/*.jar

5) Chạy frontend (tĩnh)

   - Option A — VS Code Live Server: mở thư mục `frontend` và dùng `Open with Live Server`. Đảm bảo Live Server phục vụ từ workspace gốc để đường dẫn `http://127.0.0.1:5501/frontend/bus-ticket-web` hoạt động.
   - Option B — Python simple server (port 5501):

     cd <project-root>
     python -m http.server 5501

   - Sau khi frontend chạy, truy cập:

     http://127.0.0.1:5501/frontend/bus-ticket-web

6) Kiểm thử nhanh

   - Kiểm tra API backend: `http://localhost:8080/actuator/health` (nếu actuator được bật) hoặc gọi một endpoint API.
   - Chạy test unit backend:

     mvnw.cmd test  (Windows)
     ./mvnw test   (Linux/macOS)

7) Các lưu ý bảo mật

   - Thay mọi mật khẩu, API keys và mail credentials trong `application.properties` bằng giá trị an toàn hoặc dùng biến môi trường.
   - Không đẩy thông tin nhạy cảm lên remote repo.

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

## 9.1 Cấu trúc như mục Backend Spring boot

```text
src/main/java/com/busticket/api/
├── BusTicketApiApplication.java   # File khởi chạy chính của ứng dụng Spring Boot
├── controller/                    # Nhận request từ frontend và trả response API
├── service/                       # Xử lý logic nghiệp vụ của hệ thống
├── repository/                    # Làm việc với database thông qua Spring Data JPA
├── entity/                        # Ánh xạ các bảng trong database thành class Java
├── dto/                           # Định nghĩa dữ liệu request/response giữa frontend và backend
├── config/                        # Cấu hình hệ thống như CORS, Security, JWT
└── exception/                     # Xử lý lỗi tập trung cho toàn bộ ứng dụng

Cấu trúc này giúp tách riêng tài liệu, mã nguồn và các script cơ sở dữ liệu để dễ quản lý, triển khai và bảo trì.


````




## 11. Ví dụ sử dụng

### 11.1. Đặt vé

Người dùng tìm chuyến xe phù hợp, chọn ghế trống và tiến hành đặt vé. Hệ thống sẽ:

1. Kiểm tra ghế còn trống hay không
2. Tạo bản ghi đặt vé
3. Tạo vé tương ứng
4. Tạo hóa đơn
5. Đưa vé vào trạng thái giữ chỗ trong 15 phút nếu chưa thanh toán

### 11.2. Thanh toán

Khi khách hàng thanh toán thành công:

1. Hệ thống ghi nhận giao dịch vào bảng `THANHTOAN`
2. Trigger cập nhật trạng thái hóa đơn
3. Trạng thái đơn đặt vé và vé được cập nhật tương ứng

### 11.3. Hủy vé

Khi khách hàng hủy vé:

- Hệ thống xác định thời gian còn lại trước giờ khởi hành
- Áp dụng chính sách hoàn tiền tương ứng: **100%**, **50%** hoặc **0%**
- Cập nhật trạng thái đơn và vé liên quan

---

## 12. Công nghệ sử dụng

### Kiến trúc công nghệ

- **Database:** Oracle Database 19c/21c (Triggers, Stored Procedures, Scheduler Jobs)
- **Backend:** Java 17, Spring Boot 3.x, Spring Data JPA
- **Frontend:** HTML5, CSS3, JavaScript, Bootstrap 5
- **Security:** JWT (JSON Web Token), BCrypt Password Hashing


## 13. Lưu ý quan trọng

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
