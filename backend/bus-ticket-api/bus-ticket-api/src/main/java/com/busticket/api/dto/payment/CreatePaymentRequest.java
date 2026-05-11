package com.busticket.api.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRequest {

  private String maDatVe;

  private String maKhuyenMai;
}