package com.busticket.api.dto.staffxe;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class StaffUpdateChuyenXeRequest {

  private String maXe;

  private String maBenDi;
  private String maBenDen;

  private LocalDate ngayDi;
  private LocalTime gioDi;

  private BigDecimal giaVe;

  private Integer khoangCach;
  private Integer thoiGianDuKien;

  private List<StaffTripStopRequest> stops;
}