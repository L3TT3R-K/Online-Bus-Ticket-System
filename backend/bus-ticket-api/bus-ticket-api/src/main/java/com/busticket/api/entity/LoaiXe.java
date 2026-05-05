package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "LOAIXE")
@Getter
@Setter
public class LoaiXe {

  @Id
  @Column(name = "MALOAIXE", length = 20)
  private String maLoaiXe;

  @Column(name = "TENLOAIXE", nullable = false, length = 100)
  private String tenLoaiXe;

  @Column(name = "MOTA", length = 500)
  private String moTa;
}