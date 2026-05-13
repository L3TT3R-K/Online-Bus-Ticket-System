package com.busticket.api.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAdminAccountStatusRequest {

  private String trangThaiTK;
  private String trangThai;
}
