package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TUYENXE")
public class TuyenXe {

  @Id
  @Column(name = "MATUYEN", length = 20)
  private String maTuyen;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MABENDI", nullable = false)
  private BenXe benDi;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MABENDEN", nullable = false)
  private BenXe benDen;

  @Column(name = "KHOANGCACH")
  private Integer khoangCach;

  @Column(name = "THOIGIANDUKIEN")
  private Integer thoiGianDuKien;
}