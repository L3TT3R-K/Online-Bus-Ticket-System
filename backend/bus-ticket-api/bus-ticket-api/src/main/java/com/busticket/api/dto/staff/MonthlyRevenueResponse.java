package com.busticket.api.dto.staff;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlyRevenueResponse {

  private String month;

  private Integer monthNumber;

  private BigDecimal revenue;
}