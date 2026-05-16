package com.busticket.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminDiemBenResponse {

  private String maDiemBen;
  private String tenDiem;
  private String diaChi;
  private String loai;
  private Integer thuTu;
  private String trangThai;
}
