package com.busticket.api.dto.chuyenxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    private List<String> images;

    private List<String> amenities;

    private List<ChuyenXeDiemDonTraResponse> diemDon;

    private List<ChuyenXeDiemDonTraResponse> diemTra;

    private List<ChuyenXeKhuyenMaiResponse> khuyenMai;

    private Double rating;

    private Integer reviewCount;

    private String trangThai;
}
