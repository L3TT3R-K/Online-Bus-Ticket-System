package com.busticket.api.controller;

import com.busticket.api.dto.admin.AdminAccountResponse;
import com.busticket.api.dto.admin.CreateAdminAccountRequest;
import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminAccountController {

  private final AdminAccountService adminAccountService;

  @GetMapping
  public ResponseEntity<?> getAccounts() {
    try {
      List<AdminAccountResponse> response = adminAccountService.getAccounts();
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping
  public ResponseEntity<?> createAccount(@RequestBody CreateAdminAccountRequest request) {
    try {
      AdminAccountResponse response = adminAccountService.createAccount(request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }
}
