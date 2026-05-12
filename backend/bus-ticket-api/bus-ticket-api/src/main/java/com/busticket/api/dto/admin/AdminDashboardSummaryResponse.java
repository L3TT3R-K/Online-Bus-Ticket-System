package com.busticket.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class AdminDashboardSummaryResponse {

  private Long totalAccounts;
  private Long totalCustomers;
  private Long totalCompanies;
  private Long totalTrips;
  private Long totalTickets;
  private BigDecimal totalRevenue;
}
