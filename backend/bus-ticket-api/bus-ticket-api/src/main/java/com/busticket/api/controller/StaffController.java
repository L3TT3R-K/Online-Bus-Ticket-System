package com.busticket.api.controller;

import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.dto.staff.StaffDashboardResponse;
import com.busticket.api.dto.staff.StaffMeResponse;
import com.busticket.api.service.StaffDashboardService;
import com.busticket.api.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StaffController {

  private final StaffService staffService;
  private final StaffDashboardService staffDashboardService;

  @GetMapping("/me")
  public ResponseEntity<?> getCurrentStaff(
          @RequestHeader("X-MaTK") Long maTK
  ) {
    try {
      StaffMeResponse response = staffService.getCurrentStaff(maTK);
      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @GetMapping("/dashboard")
  public ResponseEntity<?> getDashboard(
          @RequestHeader("X-MaTK") Long maTK
  ) {
    try {
      StaffDashboardResponse response = staffDashboardService.getDashboard(maTK);
      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @GetMapping("/revenue/monthly")
  public ResponseEntity<?> getMonthlyRevenue(
          @RequestParam(defaultValue = "2026") Integer year,
          @RequestHeader("X-MaTK") Long maTK
  ) {
    try {
      return ResponseEntity.ok(
              staffDashboardService.getMonthlyRevenue(maTK, year)
      );

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }
}