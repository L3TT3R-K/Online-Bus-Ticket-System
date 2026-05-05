package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "XE")
@Getter
@Setter
public class Xe {

  @Id
  @Column(name = "MAXE", length = 20)
  private String maXe;

  @Column(name = "MANHAXE", nullable = false, length = 20)
  private String maNhaXe;

  @Column(name = "BIENSO", nullable = false, length = 20)
  private String bienSo;

  @Column(name = "MALOAIXE", nullable = false, length = 20)
  private String maLoaiXe;

  @Column(name = "SOLUONGGHE", nullable = false)
  private Integer soLuongGhe;

  @Column(name = "TRANGTHAI", nullable = false, length = 30)
  private String trangThai;
}