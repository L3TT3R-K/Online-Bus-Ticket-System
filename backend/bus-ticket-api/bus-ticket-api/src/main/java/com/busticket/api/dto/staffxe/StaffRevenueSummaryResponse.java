package com.busticket.api.dto.staffxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class StaffRevenueSummaryResponse {

  private BigDecimal totalRevenue;

  private Long paidTicketCount;

  private String topTripId;

  private BigDecimal topTripRevenue;
}