package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "DATVE")
public class DatVe {

  @Id
  @Column(name = "MADATVE", length = 20)
  private String maDatVe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MAKH")
  private KhachHang khachHang;

  @Column(name = "NGAYDAT")
  private LocalDateTime ngayDat;

  @Column(name = "TRANGTHAI", length = 30)
  private String trangThai;

  @PrePersist
  public void prePersist() {
    if (ngayDat == null) {
      ngayDat = LocalDateTime.now();
    }

    if (trangThai == null) {
      trangThai = "Chờ thanh toán";
    }
  }
}