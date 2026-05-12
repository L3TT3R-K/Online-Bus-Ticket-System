package com.busticket.api.dto.ve;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class KhachHangVeResponse {

  private String maVe;
  private String maDatVe;
  private String tenKhachHang;
  private String maChuyen;
  private String tenTuyen;
  private LocalDateTime thoiGianKhoiHanh;
  private LocalDateTime thoiGianDen;
  private String maNhaXe;
  private String tenNhaXe;
  private String soGhe;
  private String maDiemDon;
  private String tenDiemDon;
  private String maDiemTra;
  private String tenDiemTra;
  private BigDecimal giaTien;
  private LocalDateTime thoiGianDat;
  private String trangThaiVe;
}
