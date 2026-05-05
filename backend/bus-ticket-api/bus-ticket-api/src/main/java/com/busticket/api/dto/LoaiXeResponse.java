package com.busticket.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoaiXeResponse {

  private String maLoaiXe;

  private String tenLoaiXe;
}