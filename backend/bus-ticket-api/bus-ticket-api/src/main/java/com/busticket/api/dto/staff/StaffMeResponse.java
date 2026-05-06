package com.busticket.api.dto.staff;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StaffMeResponse {

  private boolean success;

  private String message;

  private Long maTK;

  private String tenDangNhap;

  private String quyen;

  private String maNV;

  private String tenNV;

  private String sdt;

  private String email;

  private String trangThaiNhanVien;

  private String maNhaXe;

  private String tenNhaXe;
}