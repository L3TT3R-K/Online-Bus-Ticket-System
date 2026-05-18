package com.busticket.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AdminAccountResponse {

  private Long maTK;
  private String tenDangNhap;
  private String quyen;
  private String trangThaiTK;
  private LocalDateTime ngayTao;
  private String maNguoiDung;
  private String tenNguoiDung;
  private String email;
  private String sdt;
  private String loaiHoSo;
  private String maNhaXe;
  private String tenNhaXe;
}
