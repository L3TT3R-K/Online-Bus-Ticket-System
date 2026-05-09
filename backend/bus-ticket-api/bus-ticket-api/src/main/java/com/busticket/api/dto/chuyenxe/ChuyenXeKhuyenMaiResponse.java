package com.busticket.api.dto.chuyenxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ChuyenXeKhuyenMaiResponse {

    private String maKhuyenMai;

    private String tenKhuyenMai;

    private BigDecimal phanTramGiam;

    private BigDecimal soTienGiam;

    private LocalDateTime ngayBatDau;

    private LocalDateTime ngayKetThuc;
}
