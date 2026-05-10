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
  @JoinColumn(name = "MADATVE")
  private DatVe datVe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MACHUYEN", nullable = false)
  private ChuyenXe chuyenXe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MAGHE", nullable = false)
  private Ghe ghe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MAKH")
  private KhachHang khachHang;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MALOAIVE", nullable = false)
  private LoaiVe loaiVe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MADIEMDON")
  private DiemDonTra diemDon;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MADIEMTRA")
  private DiemDonTra diemTra;

  @Column(name = "GIATIEN", nullable = false)
  private BigDecimal giaTien;

  @Column(name = "TRANGTHAI", nullable = false, length = 50)
  private String trangThai;

  @Column(name = "THOIGIANDAT")
  private LocalDateTime thoiGianDat;

  @Column(name = "THOIGIANGIUDEN")
  private LocalDateTime thoiGianGiuDen;

  @PrePersist
  public void prePersist() {
    if (thoiGianDat == null) {
      thoiGianDat = LocalDateTime.now();
    }

    if (trangThai == null) {
      trangThai = "Giữ chỗ";
    }
  }
}