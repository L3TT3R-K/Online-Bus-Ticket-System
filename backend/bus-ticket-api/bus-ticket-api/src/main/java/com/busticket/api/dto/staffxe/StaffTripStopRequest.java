package com.busticket.api.dto.staffxe;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffTripStopRequest {

  private String maDiemBen;
  private String name;
  private String type;
  private Integer order;
}