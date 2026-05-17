package com.busticket.api.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateAdminCustomerRequest {

  private String tenDangNhap;
  private String matKhau;
  private String trangThaiTK;

  private String tenKH;
  private LocalDate ngaySinh;
  private String gioiTinh;
  private String sdt;
  private String email;
  private String trangThai;
}
