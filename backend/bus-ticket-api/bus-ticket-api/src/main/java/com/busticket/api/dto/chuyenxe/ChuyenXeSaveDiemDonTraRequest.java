package com.busticket.api.dto.chuyenxe;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChuyenXeSaveDiemDonTraRequest {

  private List<ChuyenXeDiemDonTraRequest> diemDon;

  private List<ChuyenXeDiemDonTraRequest> diemTra;
}
