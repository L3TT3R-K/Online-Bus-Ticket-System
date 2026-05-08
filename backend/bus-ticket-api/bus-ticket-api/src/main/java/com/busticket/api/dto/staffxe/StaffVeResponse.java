package com.busticket.api.dto.staffxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class StaffVeResponse {

  private String maVe;

  private String maChuyen;
  private String tenTuyen;
  private LocalDateTime thoiGianKhoiHanh;

  private String soGhe;

  private String maKH;
  private String tenKhachHang;
  private String soDienThoai;

  private BigDecimal giaTien;

  private LocalDateTime thoiGianDat;

  private String trangThaiVe;
  private String trangThaiThanhToan;
}