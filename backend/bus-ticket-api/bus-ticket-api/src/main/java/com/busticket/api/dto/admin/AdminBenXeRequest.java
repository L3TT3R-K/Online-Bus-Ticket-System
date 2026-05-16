package com.busticket.api.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminBenXeRequest {

  private String maBen;
  private String tenBen;
  private String diaChi;
  private List<AdminDiemBenRequest> diemBen;
}
