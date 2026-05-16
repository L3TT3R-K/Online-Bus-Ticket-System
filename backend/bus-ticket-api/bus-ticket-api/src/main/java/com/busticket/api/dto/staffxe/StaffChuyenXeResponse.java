package com.busticket.api.dto.staffxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class StaffChuyenXeResponse {

  private String maChuyen;

  private String maXe;
  private String bienSo;
  private String maLoaiXe;
  private String tenLoaiXe;
  private Integer soLuongGhe;
  private String maNhaXe;
  private String tenNhaXe;

  private String maTuyen;

  private String maBenDi;
  private String tenBenDi;

  private String maBenDen;
  private String tenBenDen;

  private String tenTuyen;

  private LocalDate ngayDi;
  private LocalTime gioDi;

  private BigDecimal giaVe;
  private Integer gheTrong;

  private String trangThai;

  private Integer khoangCach;
  private Integer thoiGianDuKien;

  private List<StaffDiemDonTraResponse> stops;
}
