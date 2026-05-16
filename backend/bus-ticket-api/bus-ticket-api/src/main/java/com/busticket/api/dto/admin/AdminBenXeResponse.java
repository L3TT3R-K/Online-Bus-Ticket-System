package com.busticket.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AdminBenXeResponse {

  private String maBen;
  private String tenBen;
  private String diaChi;
  private List<AdminDiemBenResponse> diemBen;
}
