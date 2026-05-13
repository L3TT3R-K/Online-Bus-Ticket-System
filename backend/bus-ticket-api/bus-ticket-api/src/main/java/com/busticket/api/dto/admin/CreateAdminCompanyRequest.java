package com.busticket.api.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAdminCompanyRequest {

  private String maNhaXe;
  private String tenNhaXe;
  private String sdt;
  private String email;
  private String diaChi;
  private String moTa;
  private String trangThai;
}
