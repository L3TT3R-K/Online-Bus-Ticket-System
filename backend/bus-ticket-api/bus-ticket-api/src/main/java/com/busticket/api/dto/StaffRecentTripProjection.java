package com.busticket.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface StaffRecentTripProjection {

  String getMaChuyen();

  String getBienSo();

  String getTuyen();

  LocalDateTime getThoiGianKhoiHanh();

  BigDecimal getGiaVe();

  Integer getSoGheTrong();

  String getTrangThai();
}