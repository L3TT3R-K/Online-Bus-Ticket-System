package com.busticket.api.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreatePaymentResponse {

  private String maDatVe;

  private String maHoaDon;

  private String maThanhToan;

  private Long orderCode;

  private String checkoutUrl;

  private String paymentLinkId;

  private String trangThai;
}