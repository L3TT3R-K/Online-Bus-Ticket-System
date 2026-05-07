package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "DIEMDONTRA")
public class DiemDonTra {

  @Id
  @Column(name = "MADIEM", length = 20)
  private String maDiem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MACHUYEN", nullable = false)
  private ChuyenXe chuyenXe;

  @Column(name = "TENDIEM", length = 200)
  private String tenDiem;

  @Column(name = "THOIGIAN")
  private LocalDateTime thoiGian;

  @Column(name = "LOAI", length = 10)
  private String loai;

  @Column(name = "THUTU")
  private Integer thuTu;
}