package com.busticket.api.controller;

import com.busticket.api.dto.admin.AdminCompanyResponse;
import com.busticket.api.dto.admin.CreateAdminCompanyRequest;
import com.busticket.api.dto.admin.UpdateAdminCompanyRequest;
import com.busticket.api.dto.admin.UpdateAdminCompanyStatusRequest;
import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.service.AdminCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminCompanyController {

  private final AdminCompanyService adminCompanyService;

  @GetMapping
  public ResponseEntity<?> getCompanies() {
    try {
      List<AdminCompanyResponse> response = adminCompanyService.getCompanies();
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping
  public ResponseEntity<?> createCompany(@RequestBody CreateAdminCompanyRequest request) {
    try {
      AdminCompanyResponse response = adminCompanyService.createCompany(request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maNhaXe}")
  public ResponseEntity<?> updateCompany(
          @PathVariable String maNhaXe,
          @RequestBody UpdateAdminCompanyRequest request
  ) {
    try {
      AdminCompanyResponse response = adminCompanyService.updateCompany(maNhaXe, request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maNhaXe}/status")
  public ResponseEntity<?> updateCompanyStatus(
          @PathVariable String maNhaXe,
          @RequestBody UpdateAdminCompanyStatusRequest request
  ) {
    try {
      AdminCompanyResponse response = adminCompanyService.updateCompanyStatus(maNhaXe, request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @DeleteMapping("/{maNhaXe}")
  public ResponseEntity<?> deleteCompany(@PathVariable String maNhaXe) {
    try {
      adminCompanyService.deleteCompany(maNhaXe);
      return ResponseEntity.ok(new ApiResponse(true, "Da xoa nha xe."));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }
}
