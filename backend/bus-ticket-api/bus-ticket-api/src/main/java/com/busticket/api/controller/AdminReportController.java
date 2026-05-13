package com.busticket.api.controller;

import com.busticket.api.dto.admin.AdminDashboardSummaryResponse;
import com.busticket.api.dto.admin.AdminTopCompanyResponse;
import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminReportController {

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

  @GetMapping("/company-revenue")
  public ResponseEntity<?> getCompanyRevenue() {
    try {
      List<AdminTopCompanyResponse> response = adminDashboardService.getCompanyRevenueReport();
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }
}
