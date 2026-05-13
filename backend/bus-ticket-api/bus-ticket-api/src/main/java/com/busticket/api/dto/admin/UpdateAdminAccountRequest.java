package com.busticket.api.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateAdminAccountRequest {

  private String tenDangNhap;
  private String matKhau;
  private String quyen;
  private String trangThaiTK;

  private String tenNguoiDung;
  private String email;
  private String sdt;
  private LocalDate ngaySinh;
  private String gioiTinh;

  private String maNhaXe;
  private LocalDate ngayVaoLam;
}
