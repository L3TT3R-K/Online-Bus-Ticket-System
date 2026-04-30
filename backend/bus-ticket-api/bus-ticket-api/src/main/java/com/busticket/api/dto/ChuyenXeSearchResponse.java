package com.busticket.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ChuyenXeSearchResponse {

    private String maChuyen;

    private String tenNhaXe;

    private String bienSo;

    private String tenLoaiXe;

    private String diemDi;

    private String diemDen;

    private LocalDateTime thoiGianKhoiHanh;

    private LocalDateTime thoiGianDen;

    private BigDecimal giaVe;

    private Integer soLuongGhe;

    private Integer soGheDaDat;

    private Integer soGheTrong;

    private String trangThai;
}