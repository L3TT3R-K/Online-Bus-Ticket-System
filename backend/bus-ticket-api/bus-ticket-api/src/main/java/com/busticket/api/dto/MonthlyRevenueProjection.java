package com.busticket.api.dto;

import java.math.BigDecimal;

public interface MonthlyRevenueProjection {

  Integer getMonthNumber();

  BigDecimal getRevenue();
}