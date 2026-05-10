package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "LOAIVE")
public class LoaiVe {

  @Id
  @Column(name = "MALOAIVE", length = 20)
  private String maLoaiVe;

  @Column(name = "TENLOAIVE", nullable = false, length = 100)
  private String tenLoaiVe;

  @Column(name = "HESOGIA", nullable = false)
  private BigDecimal heSoGia;

  @Column(name = "MOTA", length = 300)
  private String moTa;

  @Column(name = "TRANGTHAI", nullable = false, length = 20)
  private String trangThai;
}