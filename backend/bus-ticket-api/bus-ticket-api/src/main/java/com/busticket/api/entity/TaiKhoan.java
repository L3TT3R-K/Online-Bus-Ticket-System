package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TAIKHOAN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MATK")
    private Long maTK;

    @Column(name = "TENDANGNHAP", nullable = false, unique = true, length = 50)
    private String tenDangNhap;

    @Column(name = "MATKHAU", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "QUYEN", nullable = false, length = 20)
    private String quyen;

    @Column(name = "TRANGTHAITK", nullable = false, length = 20)
    private String trangThaiTK;

    @Column(name = "NGAYTAO", nullable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    public void prePersist() {
        if (quyen == null) {
            quyen = "KhachHang";
        }
        if (trangThaiTK == null) {
            trangThaiTK = "Hoạt động";
        }
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }
}