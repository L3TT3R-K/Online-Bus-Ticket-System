package com.busticket.api.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAdminBookingRequest {

  private String trangThaiDatVe;
  private String trangThaiHoaDon;
  private String trangThaiThanhToan;
}
