package com.busticket.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AdminCustomerResponse {

  private String maKH;
  private String tenKH;
  private LocalDate ngaySinh;
  private String gioiTinh;
  private String sdt;
  private String email;
  private String trangThai;
  private Long maTK;
  private String tenDangNhap;
  private String trangThaiTK;
  private LocalDateTime ngayTao;
}
