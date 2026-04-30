package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CHUYENXE")
@Getter
@Setter
public class ChuyenXe {

    @Id
    @Column(name = "MACHUYEN", length = 20)
    private String maChuyen;

    @Column(name = "MAXE", nullable = false, length = 20)
    private String maXe;

    @Column(name = "MATUYEN", nullable = false, length = 20)
    private String maTuyen;

    @Column(name = "THOIGIANKHOIHANH", nullable = false)
    private LocalDateTime thoiGianKhoiHanh;

    @Column(name = "THOIGIANDEN", nullable = false)
    private LocalDateTime thoiGianDen;

    @Column(name = "GIAVE", nullable = false)
    private BigDecimal giaVe;

    @Column(name = "TRANGTHAI", nullable = false, length = 20)
    private String trangThai;
}