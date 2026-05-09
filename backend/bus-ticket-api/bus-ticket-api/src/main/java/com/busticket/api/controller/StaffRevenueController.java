package com.busticket.api.controller;

import com.busticket.api.dto.staffxe.StaffRevenueSummaryResponse;
import com.busticket.api.service.StaffRevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff/revenue")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StaffRevenueController {

  private final StaffRevenueService staffRevenueService;

  @GetMapping("/summary")
  public StaffRevenueSummaryResponse getRevenueSummary(
          @RequestHeader("X-MaTK") Long maTK
  ) {
    return staffRevenueService.getRevenueSummary(maTK);
  }
}