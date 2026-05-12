package com.busticket.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "DANHGIA")
public class DanhGia {

  @Id
  @Column(name = "MADANHGIA", length = 20)
  private String maDanhGia;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MAKH", nullable = false)
  private KhachHang khachHang;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MACHUYEN", nullable = false)
  private ChuyenXe chuyenXe;

  @Column(name = "SOSAO", nullable = false)
  private Integer soSao;

  @Lob
  @Column(name = "NOIDUNG")
  private String noiDung;

  @Column(name = "NGAYDANHGIA", nullable = false)
  private LocalDateTime ngayDanhGia;

  @PrePersist
  public void prePersist() {
    if (ngayDanhGia == null) {
      ngayDanhGia = LocalDateTime.now();
    }
  }
}
