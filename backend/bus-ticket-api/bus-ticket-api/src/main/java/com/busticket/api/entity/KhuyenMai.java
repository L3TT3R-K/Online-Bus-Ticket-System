package com.busticket.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "KHUYENMAI")
@Getter
@Setter
public class KhuyenMai {

  @Id
  @Column(name = "MAKHUYENMAI", length = 20)
  private String maKhuyenMai;

  @Column(name = "TENKHUYENMAI", nullable = false, length = 100)
  private String tenKhuyenMai;

  @Column(name = "PHANTRAMGIAM")
  private BigDecimal phanTramGiam;

  @Column(name = "SOTIENGIAM")
  private BigDecimal soTienGiam;

  @Column(name = "NGAYBATDAU")
  private LocalDateTime ngayBatDau;

  @Column(name = "NGAYKETTHUC")
  private LocalDateTime ngayKetThuc;

  @Column(name = "TRANGTHAI", length = 20)
  private String trangThai;
}
