package com.busticket.api.dto.datve;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CreateDatVeTicketResponse {

  private String maVe;
  private String maGhe;
  private BigDecimal giaTien;
  private String trangThai;
}
