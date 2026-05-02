package com.busticket.api.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {
  private String matKhauCu;

  private String matKhauMoi;

  private String xacNhanMatKhauMoi;
}
