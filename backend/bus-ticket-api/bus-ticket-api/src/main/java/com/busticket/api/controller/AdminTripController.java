package com.busticket.api.controller;

import com.busticket.api.dto.admin.AdminTripResponse;
import com.busticket.api.dto.admin.UpdateAdminTripStatusRequest;
import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.service.AdminTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminTripController {

  private final AdminTripService adminTripService;

  @GetMapping
  public ResponseEntity<?> getTrips() {
    try {
      List<AdminTripResponse> response = adminTripService.getTrips();
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maChuyen}/status")
  public ResponseEntity<?> updateTripStatus(
          @PathVariable String maChuyen,
          @RequestBody UpdateAdminTripStatusRequest request
  ) {
    try {
      AdminTripResponse response = adminTripService.updateTripStatus(maChuyen, request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }
}
