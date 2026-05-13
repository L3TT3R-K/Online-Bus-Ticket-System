package com.busticket.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AdminStaffResponse {

  private String maNV;
  private String tenNV;
  private String gioiTinh;
  private String sdt;
  private String email;
  private LocalDate ngayVaoLam;
  private String trangThai;
  private String maNhaXe;
  private String tenNhaXe;
  private Long maTK;
  private String tenDangNhap;
  private String trangThaiTK;
  private LocalDateTime ngayTao;
}
