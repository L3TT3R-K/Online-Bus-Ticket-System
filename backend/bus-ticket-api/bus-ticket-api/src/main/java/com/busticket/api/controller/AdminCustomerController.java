package com.busticket.api.controller;

import com.busticket.api.dto.admin.AdminCustomerResponse;
import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.service.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminCustomerController {

  private final AdminCustomerService adminCustomerService;

  @GetMapping
  public ResponseEntity<?> getCustomers() {
    try {
      List<AdminCustomerResponse> response = adminCustomerService.getCustomers();
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }
}
