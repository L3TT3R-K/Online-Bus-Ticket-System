package com.busticket.api.dto.staffxe;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StaffXeResponse {

  private String maXe;

  private String bienSo;

  private String maLoaiXe;

  private String tenLoaiXe;

  private Integer soLuongGhe;

  private String trangThai;

  private List<String> images;

  private String imageDesc;

  private List<String> amenities;
}