package com.busticket.api.controller;

import com.busticket.api.dto.admin.AdminDashboardSummaryResponse;
import com.busticket.api.dto.admin.AdminTopCompanyResponse;
import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.dto.staff.MonthlyRevenueResponse;
import com.busticket.api.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminDashboardController {

  private final AdminDashboardService adminDashboardService;

  @GetMapping("/summary")
  public ResponseEntity<?> getSummary() {
    try {
      AdminDashboardSummaryResponse response = adminDashboardService.getSummary();
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @GetMapping("/revenue-monthly")
  public ResponseEntity<?> getRevenueMonthly(
          @RequestParam(value = "year", required = false) Integer year
  ) {
    try {
      List<MonthlyRevenueResponse> response = adminDashboardService.getRevenueMonthly(year);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @GetMapping("/top-companies")
  public ResponseEntity<?> getTopCompanies(
          @RequestParam(value = "limit", required = false) Integer limit
  ) {
    try {
      List<AdminTopCompanyResponse> response = adminDashboardService.getTopCompanies(limit);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }
}
