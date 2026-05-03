package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "NHAXE")
@Getter
@Setter
public class NhaXe {

  @Id
  @Column(name = "MANHAXE", length = 20)
  private String maNhaXe;

  @Column(name = "TENNHAXE", nullable = false, length = 100)
  private String tenNhaXe;

  @Column(name = "SDT", length = 15)
  private String sdt;

  @Column(name = "EMAIL", length = 100)
  private String email;

  @Column(name = "DIACHI", length = 200)
  private String diaChi;

  @Lob
  @Column(name = "MOTA")
  private String moTa;
}