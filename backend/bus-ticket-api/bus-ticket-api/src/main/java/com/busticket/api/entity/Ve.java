package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "VE")
public class Ve {

  @Id
  @Column(name = "MAVE", length = 20)
  private String maVe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MACHUYEN")
  private ChuyenXe chuyenXe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MAKH")
  private KhachHang khachHang;

  @Column(name = "SOGHE", length = 10)
  private String soGhe;

  @Column(name = "GIATIEN")
  private BigDecimal giaTien;

  @Column(name = "THOIGIANDAT")
  private LocalDateTime thoiGianDat;

  @Column(name = "TRANGTHAI", length = 30)
  private String trangThai;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MADIEMDON")
  private DiemDonTra diemDon;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MADIEMTRA")
  private DiemDonTra diemTra;
}