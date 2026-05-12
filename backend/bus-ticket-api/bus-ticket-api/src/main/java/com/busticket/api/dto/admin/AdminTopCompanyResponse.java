package com.busticket.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class AdminTopCompanyResponse {

  private String maNhaXe;
  private String tenNhaXe;
  private Long tripCount;
  private Long paidOrderCount;
  private BigDecimal revenue;
}
