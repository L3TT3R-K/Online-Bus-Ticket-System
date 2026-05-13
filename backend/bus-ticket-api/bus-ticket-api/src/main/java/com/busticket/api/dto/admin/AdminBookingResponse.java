package com.busticket.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AdminBookingResponse {

  private String maDatVe;
  private LocalDateTime ngayDat;
  private String trangThaiDatVe;
  private String maKH;
  private String tenKH;
  private String email;
  private String sdt;
  private String maChuyen;
  private String tenNhaXe;
  private String bienSo;
  private String tenBenDi;
  private String tenBenDen;
  private LocalDateTime thoiGianKhoiHanh;
  private LocalDateTime thoiGianDen;
  private Integer soLuongVe;
  private List<String> maVe;
  private List<String> soGhe;
  private BigDecimal giaGoc;
  private BigDecimal tienGiam;
  private BigDecimal tongTien;
  private String maHoaDon;
  private String trangThaiHoaDon;
  private String maThanhToan;
  private Long orderCode;
  private String phuongThucThanhToan;
  private String trangThaiThanhToan;
  private LocalDateTime ngayThanhToan;
}
