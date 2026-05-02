package com.busticket.api.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateAccountRequest {

  private String tenKH;

  private String sdt;

  private String email;

  private LocalDate ngaySinh;

  private String gioiTinh;
}
