package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "DIEMBEN")
public class DiemBen {

  @Id
  @Column(name = "MADIEMBEN", length = 30)
  private String maDiemBen;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MABEN", nullable = false)
  private BenXe benXe;

  @Column(name = "TENDIEM", nullable = false, length = 200)
  private String tenDiem;

  @Column(name = "DIACHI", length = 300)
  private String diaChi;

  @Column(name = "LOAI", nullable = false, length = 10)
  private String loai;

  @Column(name = "THUTU", nullable = false)
  private Integer thuTu;

  @Column(name = "TRANGTHAI", nullable = false, length = 20)
  private String trangThai;
}