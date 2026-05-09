package com.busticket.api.dto.staffxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class StaffRevenueTripResponse {

  private String maChuyen;

  private String tenTuyen;

  private String maXe;

  private String bienSo;

  private LocalDate ngayDi;

  private LocalTime gioDi;

  private Integer tongGhe;

  private Long soVeDaThanhToan;

  private Integer tyLeLapDay;

  private BigDecimal doanhThu;
}