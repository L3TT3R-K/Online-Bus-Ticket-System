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
          @RequestHeader(value = "X-MaTK", required = false) Long maTK,
          @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    try {
      Long currentMaTK = maTK;

      if (currentMaTK == null) {
        currentMaTK = extractMaTKFromDemoToken(authorization);
      }

      if (currentMaTK == null) {
        throw new RuntimeException("Thiếu mã tài khoản nhân viên");
      }

      StaffMeResponse response = staffService.getCurrentStaff(currentMaTK);
      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  private Long extractMaTKFromDemoToken(String authorization) {
    if (authorization == null || authorization.isBlank()) {
      return null;
    }

    String token = authorization.trim();

    if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
      token = token.substring(7).trim();
    }

    if (token.matches("\\d+")) {
      return Long.parseLong(token);
    }

    if (token.startsWith("demo-token-")) {
      String value = token.substring("demo-token-".length());

      if (value.matches("\\d+")) {
        return Long.parseLong(value);
      }
    }

    return null;
  }

  @GetMapping("/dashboard")
  public ResponseEntity<?> getDashboard(
          @RequestHeader(value = "X-MaTK", required = false) Long maTK,
          @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    try {
      Long currentMaTK = resolveMaTK(maTK, authorization);

      StaffDashboardResponse response = staffDashboardService.getDashboard(currentMaTK);
      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  private Long resolveMaTK(Long maTK, String authorization) {
    Long currentMaTK = maTK;

    if (currentMaTK == null) {
      currentMaTK = extractMaTKFromDemoToken(authorization);
    }

    if (currentMaTK == null) {
      throw new RuntimeException("Thiếu mã tài khoản nhân viên");
    }

    return currentMaTK;
  }

  @GetMapping("/revenue/monthly")
  public ResponseEntity<?> getMonthlyRevenue(
          @RequestParam(defaultValue = "2026") Integer year,
          @RequestHeader(value = "X-MaTK", required = false) Long maTK,
          @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    try {
      Long currentMaTK = resolveMaTK(maTK, authorization);

      return ResponseEntity.ok(
              staffDashboardService.getMonthlyRevenue(currentMaTK, year)
      );

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }
}