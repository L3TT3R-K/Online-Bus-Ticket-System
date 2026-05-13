package com.busticket.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AdminTripResponse {

  private String maChuyen;
  private String maXe;
  private String bienSo;
  private Integer soLuongGhe;
  private String trangThaiXe;
  private String maLoaiXe;
  private String tenLoaiXe;
  private String maNhaXe;
  private String tenNhaXe;
  private String maTuyen;
  private String maBenDi;
  private String tenBenDi;
  private String maBenDen;
  private String tenBenDen;
  private Integer khoangCach;
  private Integer thoiGianDuKien;
  private LocalDateTime thoiGianKhoiHanh;
  private LocalDateTime thoiGianDen;
  private BigDecimal giaVe;
  private String trangThai;
}
