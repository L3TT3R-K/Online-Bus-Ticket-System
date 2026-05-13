package com.busticket.api.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateAdminStaffRequest {

  private String tenDangNhap;
  private String matKhau;
  private String trangThaiTK;

  private String tenNV;
  private String gioiTinh;
  private String sdt;
  private String email;
  private LocalDate ngayVaoLam;
  private String trangThai;
  private String maNhaXe;
}
