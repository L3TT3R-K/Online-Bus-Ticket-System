package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "HOADON")
@Getter
@Setter
public class HoaDon {

  @Id
  @Column(name = "MAHOADON", length = 30)
  private String maHoaDon;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MADATVE", nullable = false, unique = true)
  private DatVe datVe;

  @Column(name = "NGAYLAP", nullable = false)
  private LocalDateTime ngayLap;

  @Column(name = "GIAGOC", nullable = false)
  private BigDecimal giaGoc;

  @Column(name = "TIENGIAM", nullable = false)
  private BigDecimal tienGiam;

  @Column(name = "TONGTIEN", nullable = false)
  private BigDecimal tongTien;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MAKHUYENMAI")
  private KhuyenMai khuyenMai;

  @Column(name = "TRANGTHAI", nullable = false, length = 30)
  private String trangThai;

  @PrePersist
  public void prePersist() {
    if (ngayLap == null) {
      ngayLap = LocalDateTime.now();
    }

    if (giaGoc == null) {
      giaGoc = BigDecimal.ZERO;
    }

    if (tienGiam == null) {
      tienGiam = BigDecimal.ZERO;
    }

    if (tongTien == null) {
      tongTien = BigDecimal.ZERO;
    }

    if (trangThai == null) {
      trangThai = "Chưa thanh toán";
    }
  }
}