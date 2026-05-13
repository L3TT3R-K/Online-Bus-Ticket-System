package com.busticket.api.controller;

import com.busticket.api.dto.admin.AdminBookingResponse;
import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.service.AdminBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminBookingController {

  private final AdminBookingService adminBookingService;

  @GetMapping
  public ResponseEntity<?> getBookings() {
    try {
      List<AdminBookingResponse> response = adminBookingService.getBookings();
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maVe}/cancel")
  public ResponseEntity<?> cancelBookingTicket(@PathVariable String maVe) {
    try {
      AdminBookingResponse response = adminBookingService.cancelTicket(maVe);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }
}
