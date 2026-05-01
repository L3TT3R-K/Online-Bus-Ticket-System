package com.busticket.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AccountResponse {

  private boolean success;

  private String message;

  private Long maTK;

  private String tenDangNhap;

  private String quyen;

  private String trangThaiTK;

  private String maKH;

  private String tenKH;

  private LocalDate ngaySinh;

  private String gioiTinh;

  private String sdt;

  private String email;

  private String trangThai;
}