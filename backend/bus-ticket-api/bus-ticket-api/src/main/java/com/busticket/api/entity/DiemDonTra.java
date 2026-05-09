package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "DIEMDONTRA")
@Getter
@Setter
public class DiemDonTra {

  @Id
  @Column(name = "MADIEM", length = 20)
  private String maDiem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MACHUYEN", nullable = false)
  private ChuyenXe chuyenXe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MABEN", nullable = false)
  private BenXe benXe;

  @Column(name = "TENDIEM", nullable = false, length = 200)
  private String tenDiem;

  @Column(name = "THOIGIAN")
  private LocalDateTime thoiGian;

  @Column(name = "LOAI", nullable = false, length = 10)
  private String loai;

  @Column(name = "THUTU", nullable = false)
  private Integer thuTu;
}