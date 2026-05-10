package com.busticket.api.dto.chuyenxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ChuyenXeDiemDonTraListResponse {

  private String maChuyen;

  private List<ChuyenXeDiemDonTraResponse> diemDon;

  private List<ChuyenXeDiemDonTraResponse> diemTra;
}
