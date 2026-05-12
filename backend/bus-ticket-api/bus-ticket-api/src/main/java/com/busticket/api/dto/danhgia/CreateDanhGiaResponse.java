package com.busticket.api.dto.danhgia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CreateDanhGiaResponse {

  private String maDanhGia;
  private String maKhachHang;
  private String maChuyen;
  private String maNhaXe;
  private Integer soSao;
  private String noiDung;
  private LocalDateTime ngayDanhGia;
}
