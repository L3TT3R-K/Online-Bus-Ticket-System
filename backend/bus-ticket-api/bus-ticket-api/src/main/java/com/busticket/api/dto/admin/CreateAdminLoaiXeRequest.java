package com.busticket.api.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAdminLoaiXeRequest {

  private String maLoaiXe;
  private String tenLoaiXe;
  private String moTa;
}
