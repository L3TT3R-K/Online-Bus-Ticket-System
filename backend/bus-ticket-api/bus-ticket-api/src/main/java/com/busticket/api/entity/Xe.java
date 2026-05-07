package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "XE")
public class Xe {

  @Id
  @Column(name = "MAXE", length = 20)
  private String maXe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MANHAXE", nullable = false)
  private NhaXe nhaXe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MALOAIXE", nullable = false)
  private LoaiXe loaiXe;

  @Column(name = "BIENSO", length = 20, nullable = false)
  private String bienSo;

  @Column(name = "SOLUONGGHE", nullable = false)
  private Integer soLuongGhe;

  @Column(name = "TRANGTHAI", length = 30)
  private String trangThai;
}