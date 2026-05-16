package com.busticket.api.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDiemBenRequest {

  private String maDiemBen;
  private String tenDiem;
  private String diaChi;
  private String loai;
  private Integer thuTu;
  private String trangThai;
}
