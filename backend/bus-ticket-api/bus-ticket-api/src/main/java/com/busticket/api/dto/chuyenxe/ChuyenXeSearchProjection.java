package com.busticket.api.dto.chuyenxe;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ChuyenXeSearchProjection {

    String getMaChuyen();

    String getTenNhaXe();

    String getBienSo();

    String getTenLoaiXe();

    String getDiemDi();

    String getDiemDen();

    LocalDateTime getThoiGianKhoiHanh();

    LocalDateTime getThoiGianDen();

    BigDecimal getGiaVe();

    Integer getSoLuongGhe();

    Integer getSoGheDaDat();

    Integer getSoGheTrong();

    String getImageUrls();

    String getAmenities();

    Double getRating();

    Integer getReviewCount();

    String getTrangThai();
}
