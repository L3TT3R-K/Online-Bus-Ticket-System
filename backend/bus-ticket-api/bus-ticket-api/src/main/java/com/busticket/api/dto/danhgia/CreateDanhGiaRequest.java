package com.busticket.api.dto.danhgia;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDanhGiaRequest {

  private String maKhachHang;
  private String maChuyen;
  private String maNhaXe;
  private Integer soSao;
  private String noiDung;
}
