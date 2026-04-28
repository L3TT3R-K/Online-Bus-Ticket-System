package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "KHACHHANG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHang {

    @Id
    @Column(name = "MAKH", length = 20)
    private String maKH;

    @Column(name = "TENKH", nullable = false, length = 100)
    private String tenKH;

    @Column(name = "NGAYSINH")
    private LocalDate ngaySinh;

    @Column(name = "GIOITINH", length = 10)
    private String gioiTinh;

    @Column(name = "SDT", unique = true, length = 15)
    private String sdt;

    @Column(name = "EMAIL", unique = true, length = 100)
    private String email;

    @OneToOne
    @JoinColumn(name = "MATK", nullable = false, unique = true)
    private TaiKhoan taiKhoan;

    @Column(name = "TRANGTHAI", nullable = false, length = 20)
    private String trangThai;

    @PrePersist
    public void prePersist() {
        if (trangThai == null) {
            trangThai = "Hoạt động";
        }
    }
}