package com.busticket.api.dto.benxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DiemBenResponse {

  private String maDiemBen;
  private String maBen;
  private String tenBen;
  private String tenDiem;
  private String diaChi;
  private String loai;
  private Integer thuTu;
}
