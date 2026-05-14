package com.busticket.api.dto.datve;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateDatVeRequest {

  private String maDatVe;
  private String maChuyen;
  private String maKhachHang;
  private String maDiemDon;
  private String maDiemTra;
  private String maLoaiVe;
  private String maGhe;
  private List<String> maGhes;
}
