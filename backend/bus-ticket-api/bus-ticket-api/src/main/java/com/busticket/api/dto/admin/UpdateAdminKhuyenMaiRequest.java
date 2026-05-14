package com.busticket.api.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateAdminKhuyenMaiRequest {

  private String tenKhuyenMai;
  private BigDecimal phanTramGiam;
  private BigDecimal soTienGiam;
  private LocalDateTime ngayBatDau;
  private LocalDateTime ngayKetThuc;
  private String trangThai;
}
