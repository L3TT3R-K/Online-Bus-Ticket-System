package com.busticket.api.dto.datve;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CreateDatVeResponse {

  private String maDatVe;
  private String maChuyen;
  private List<CreateDatVeTicketResponse> veList;
  private BigDecimal tongTien;
  private String trangThai;
}
