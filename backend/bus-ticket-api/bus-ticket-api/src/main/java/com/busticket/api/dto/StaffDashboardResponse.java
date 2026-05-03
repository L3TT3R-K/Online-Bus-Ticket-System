package com.busticket.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class StaffDashboardResponse {

  private boolean success;

  private String message;

  private String maNhaXe;

  private String tenNhaXe;

  private Long totalBus;

  private Long totalTrip;

  private Long totalTicket;

  private BigDecimal totalRevenue;

  private List<StaffRecentTripResponse> recentTrips;
}