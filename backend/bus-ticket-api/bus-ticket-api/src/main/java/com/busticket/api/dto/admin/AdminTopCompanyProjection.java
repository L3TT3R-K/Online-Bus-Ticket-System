package com.busticket.api.dto.admin;

import java.math.BigDecimal;

public interface AdminTopCompanyProjection {

  String getMaNhaXe();

  String getTenNhaXe();

  Long getTripCount();

  Long getPaidOrderCount();

  BigDecimal getRevenue();
}
