package com.busticket.api.dto.chuyenxe;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChuyenXeDiemDonTraRequest {

  private String maDiemBen;

  private String maBen;

  private String tenDiem;

  private Integer thuTu;

  private LocalDateTime thoiGian;
}
