package com.busticket.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StaffRecentTripResponse {

  private String maChuyen;

  private String bienSo;

  private String tuyen;

  private LocalDateTime thoiGianKhoiHanh;

  private BigDecimal giaVe;

  private Integer soGheTrong;

  private String trangThai;
}