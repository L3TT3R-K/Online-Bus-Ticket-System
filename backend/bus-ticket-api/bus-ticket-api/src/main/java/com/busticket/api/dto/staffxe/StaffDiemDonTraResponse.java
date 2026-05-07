package com.busticket.api.dto.staffxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class StaffDiemDonTraResponse {

  private String maDiem;
  private String tenDiem;
  private String loai;
  private Integer thuTu;
  private LocalDateTime thoiGian;
}