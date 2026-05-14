package com.busticket.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AdminKhuyenMaiResponse {

  private String maKhuyenMai;
  private String tenKhuyenMai;
  private BigDecimal phanTramGiam;
  private BigDecimal soTienGiam;
  private LocalDateTime ngayBatDau;
  private LocalDateTime ngayKetThuc;
  private String trangThai;
}
