package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "THANHTOAN")
public class ThanhToan {

  @Id
  @Column(name = "MATHANHTOAN", length = 30)
  private String maThanhToan;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MAHOADON", nullable = false)
  private HoaDon hoaDon;

  @Column(name = "ORDERCODE", nullable = false, unique = true)
  private Long orderCode;

  @Column(name = "PAYOSPAYMENTLINKID", length = 100)
  private String payOSPaymentLinkId;

  @Column(name = "CHECKOUTURL", length = 1000)
  private String checkoutUrl;

  @Column(name = "LOAIGIAODICH", nullable = false, length = 20)
  private String loaiGiaoDich;

  @Column(name = "PHUONGTHUCTHANHTOAN", nullable = false, length = 30)
  private String phuongThucThanhToan;

  @Column(name = "SOTIEN", nullable = false)
  private BigDecimal soTien;

  @Column(name = "TRANGTHAI", nullable = false, length = 30)
  private String trangThai;

  @Column(name = "NGAYTAO")
  private LocalDateTime ngayTao;

  @Column(name = "NGAYTHANHTOAN")
  private LocalDateTime ngayThanhToan;

  @Column(name = "NGAYCAPNHAT")
  private LocalDateTime ngayCapNhat;

  @PrePersist
  public void prePersist() {
    if (ngayTao == null) {
      ngayTao = LocalDateTime.now();
    }

    if (loaiGiaoDich == null) {
      loaiGiaoDich = "ThanhToan";
    }

    if (phuongThucThanhToan == null) {
      phuongThucThanhToan = "PAYOS";
    }

    if (trangThai == null) {
      trangThai = "Đang xử lý";
    }

    ngayCapNhat = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    ngayCapNhat = LocalDateTime.now();
  }
}