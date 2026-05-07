package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "CHUYENXE")
public class ChuyenXe {

    @Id
    @Column(name = "MACHUYEN", length = 20)
    private String maChuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAXE", nullable = false)
    private Xe xe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MATUYEN", nullable = false)
    private TuyenXe tuyenXe;

    @Column(name = "THOIGIANKHOIHANH", nullable = false)
    private LocalDateTime thoiGianKhoiHanh;

    @Column(name = "THOIGIANDEN", nullable = false)
    private LocalDateTime thoiGianDen;

    @Column(name = "GIAVE", nullable = false)
    private BigDecimal giaVe;

    @Column(name = "TRANGTHAI", length = 20)
    private String trangThai;
}