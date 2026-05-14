package com.busticket.api.dto.loaive;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class LoaiVeResponse {

  private String maLoaiVe;
  private String tenLoaiVe;
  private BigDecimal heSoGia;
  private String moTa;
  private String trangThai;
}
