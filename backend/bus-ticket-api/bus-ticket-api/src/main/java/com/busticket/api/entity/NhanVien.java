package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "NHANVIEN")
@Getter
@Setter
public class NhanVien {

  @Id
  @Column(name = "MANV", length = 20)
  private String maNV;

  @Column(name = "TENNV", nullable = false, length = 100)
  private String tenNV;

  @Column(name = "GIOITINH", length = 10)
  private String gioiTinh;

  @Column(name = "SDT", length = 15)
  private String sdt;

  @Column(name = "EMAIL", length = 100)
  private String email;

  @Column(name = "NGAYVAOLAM", nullable = false)
  private LocalDate ngayVaoLam;

  @Column(name = "TRANGTHAI", nullable = false, length = 20)
  private String trangThai;

  @ManyToOne
  @JoinColumn(name = "MANHAXE", nullable = false)
  private NhaXe nhaXe;

  @OneToOne
  @JoinColumn(name = "MATK", nullable = false, unique = true)
  private TaiKhoan taiKhoan;
}