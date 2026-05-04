package com.busticket.api.controller;

import com.busticket.api.dto.ApiResponse;
import com.busticket.api.dto.StaffDashboardResponse;
import com.busticket.api.dto.StaffMeResponse;
import com.busticket.api.service.StaffDashboardService;
import com.busticket.api.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    Pattern pattern = Pattern.compile("demo-token-(\\d+)");
    Matcher matcher = pattern.matcher(authorization);

    if (matcher.find()) {
      return Long.parseLong(matcher.group(1));
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