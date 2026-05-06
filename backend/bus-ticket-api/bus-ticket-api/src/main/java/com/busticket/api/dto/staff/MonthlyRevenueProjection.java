package com.busticket.api.dto.staff;

import java.math.BigDecimal;

public interface MonthlyRevenueProjection {

  Integer getMonthNumber();

  BigDecimal getRevenue();
}