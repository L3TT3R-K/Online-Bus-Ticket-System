package com.busticket.api.controller;

import com.busticket.api.dto.payment.CreatePaymentRequest;
import com.busticket.api.dto.payment.CreatePaymentResponse;
import com.busticket.api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping("/payos/create")
  public CreatePaymentResponse createPayOSPayment(
          @RequestBody CreatePaymentRequest request
  ) {
    return paymentService.createPayOSPayment(
            request.getMaDatVe(),
            request.getMaKhuyenMai()
    );
  }

  @PostMapping("/payos/webhook")
  public ResponseEntity<String> payOSWebhook(@RequestBody Map<String, Object> body) {
    paymentService.handlePayOSWebhook(body);
    return ResponseEntity.ok("OK");
  }

  @PutMapping("/payos/cancel/{orderCode}")
  public ResponseEntity<String> cancelPayment(
          @PathVariable Long orderCode
  ) {
    paymentService.cancelPayment(orderCode);
    return ResponseEntity.ok("Đã hủy thanh toán.");
  }

  @PostMapping("/payos/sync/{orderCode}")
  public ResponseEntity<String> syncPayOSPayment(
          @PathVariable Long orderCode
  ) {
    return ResponseEntity.ok(paymentService.syncPayOSPayment(orderCode));
  }
}
