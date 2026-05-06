package com.busticket.api.dto.staffxe;

import lombok.Data;

import java.util.List;

@Data
public class UpdateStaffXeRequest {

  private String bienSo;

  private String maLoaiXe;

  private Integer soLuongGhe;

  private List<String> imageUrls;

  private String imageDesc;

  private List<String> amenities;
}