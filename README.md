# 🚌 Hệ Thống Bán Vé Xe Bus Trực Tuyến

> **Nền tảng trung gian kết nối hành khách với các hãng xe khách — xây dựng trên Oracle Database**

---

## Giới thiệu

Hệ thống **Bán Vé Xe Bus Trực Tuyến** là một nền tảng trung gian (marketplace) cho phép nhiều hãng xe khách đăng ký và quản lý lịch trình của mình thông qua một cổng portal thống nhất, trong khi hành khách có thể tìm kiếm, đặt vé và thanh toán trực tuyến trên cùng một hệ thống.

Mô hình hoạt động theo hướng **Data Sync (Mô hình 2)**: hãng xe tự nhập liệu lịch trình vào hệ thống — không cần tích hợp API phức tạp — phù hợp với các hãng xe vừa và nhỏ tại Việt Nam.

---

## Tính năng chính

### Dành cho Khách hàng
- Tìm kiếm chuyến xe theo điểm đi, điểm đến và ngày khởi hành
- Xem sơ đồ ghế, chọn ghế theo vị trí mong muốn
- Đặt vé, thanh toán qua nhiều phương thức (VNPay, Momo, tiền mặt)
- Nhận vé điện tử dạng QR Code qua SMS và Email
- Hủy vé và hoàn tiền tự động theo chính sách
- Đánh giá chuyến xe sau khi đi

### Dành cho Hãng xe
- Quản lý đội xe và sơ đồ ghế
- Tạo và quản lý tuyến đường, lịch trình chuyến
- Thiết lập điểm đón/trả linh hoạt dọc tuyến
- Xem báo cáo doanh thu theo ngày/tuyến/xe
- Check-in hành khách bằng QR Code
- Upload ảnh xe phục vụ hiển thị trên nền tảng

### Dành cho Quản trị viên
- Quản lý toàn bộ hãng xe đối tác
- Thiết lập khuyến mãi (giảm % hoặc số tiền cố định)
- Đối soát và thanh toán hoa hồng định kỳ
- Theo dõi dashboard doanh thu toàn hệ thống

---

## Kiến trúc Database

Hệ thống sử dụng **Oracle Database** với **21 bảng**, được tổ chức theo 5 nhóm chức năng:

```
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
│  Đặt vé & Vé   │    │ Thanh toán &    │
│                 │    │   Khuyến mãi   │
├─────────────────┤    ├─────────────────┤
│ DATVE           │    │ HOADON          │
│ VE              │    │ THANHTOAN       │
│ LOAIVE          │    │ KHUYENMAI       │
│ BENXE           │    │ DANHGIA         │
└─────────────────┘    └─────────────────┘
```

---

## Cấu trúc thư mục



---

## Trigger (9 trigger)

| # | Tên | Bảng | Mục đích |
|---|-----|------|----------|
| 1 | `TRG_KiemTraLichXe` | CHUYENXE | Ngăn 1 xe chạy 2 chuyến cùng giờ |
| 2 | `TRG_KiemTraGheXe` | VE | Ghế phải thuộc đúng xe của chuyến |
| 3 | `TRG_KiemTraDiemDonTra` | VE | Điểm đón/trả hợp lệ, đúng thứ tự |
| 4 | `TRG_SetVeGiuCho` | VE | Tự set Giữ chỗ + timeout 15 phút |
| 5 | `TRG_CapNhatHoaDon` | HOADON | Tự tính TongTien = GiaGoc − TienGiam |
| 6 | `TRG_CapNhatDonSauThanhToan` | THANHTOAN | Cascade cập nhật HOADON → DATVE → VE |
| 7 | `TRG_HuyVeKhiDatVeHuy` | DATVE | Tự hủy vé khi đơn bị hủy |
| 8 | `TRG_KiemTraDanhGia` | DANHGIA | Chỉ đánh giá khi chuyến hoàn thành |
| 9 | `TRG_KhoaTKKhiNVNghiViec` | NHANVIEN | Khóa tài khoản NV nghỉ việc |

---

## Stored Procedure (6 SP)

| # | Tên | Mô tả |
|---|-----|-------|
| 1 | `SP_TinhTienGiam` | Tính tiền giảm khuyến mãi (% hoặc cố định) |
| 2 | `SP_DatVe` | Đặt vé: kiểm tra, tạo đơn, tạo vé, tạo hóa đơn trong 1 transaction |
| 3 | `SP_ThanhToan` | Ghi nhận thanh toán, kiểm tra số tiền, trigger cascade cập nhật |
| 4 | `SP_HuyDatVe` | Hủy vé + tính hoàn tiền (100% / 50% / 0%) theo thời gian còn lại |
| 5 | `SP_GiaiPhongGheHetHan` | Giải phóng ghế hết giờ giữ (chạy định kỳ mỗi 2 phút) |
| 6 | `SP_KiemTraVeQR` | Check-in vé QR, kiểm tra phân quyền hãng xe |

---

## Ràng buộc toàn vẹn (92 ràng buộc)

Hệ thống có 92 ràng buộc chia 5 nhóm:

| Nhóm | Số lượng | Mô tả |
|------|----------|-------|
| Khóa chính | 21 | Mỗi bảng có PK duy nhất, không null |
| Khóa ngoại | 26 | FK với quy tắc RESTRICT — không xóa khi đang được tham chiếu |
| Miền giá trị | 32 | CHECK domain, giá trị số, timestamp, UNIQUE |
| Liên thuộc tính | 9 | Ràng buộc giữa các cột trong cùng bảng hoặc liên bảng |
| Liên bộ liên quan hệ | 4 | Logic nghiệp vụ phức tạp nhiều bảng |

---

## Hướng dẫn cài đặt

### Yêu cầu
- Oracle Database 19c trở lên (hoặc Oracle XE 21c)
- SQL*Plus hoặc SQL Developer
- Quyền `CREATE TABLE`, `CREATE PROCEDURE`, `CREATE TRIGGER`, `CREATE SEQUENCE`, `CREATE VIEW`, `CREATE JOB`

### Chạy script


### Kiểm tra sau cài đặt

## Ví dụ sử dụng


## Lưu ý quan trọng

> **Bảo mật:** Script mẫu lưu mật khẩu dạng chuỗi `'hashed_...'`. Trong môi trường thực tế, phải hash bằng `DBMS_CRYPTO` hoặc xử lý ở tầng ứng dụng trước khi INSERT.

> **Scheduler Job:** Job `JOB_GIAI_PHONG_GHE` yêu cầu quyền `CREATE JOB`. Nếu không có quyền, hãy chạy thủ công `SP_GiaiPhongGheHetHan` hoặc nhờ DBA tạo job.

> **Dữ liệu mẫu:** Các mốc thời gian trong dữ liệu mẫu (tháng 7/2025) có thể cần điều chỉnh cho phù hợp với thời điểm test thực tế.

---

## Tác giả

Đồ án môn học 
Hệ thống bán vé xe bus trực tuyến
Oracle Database · HTML · CSS · Javascript · Java SpringBoot
