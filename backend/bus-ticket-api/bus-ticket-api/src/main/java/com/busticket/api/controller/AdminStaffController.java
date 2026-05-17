package com.busticket.api.controller;

import com.busticket.api.dto.admin.AdminStaffResponse;
import com.busticket.api.dto.admin.CreateAdminStaffRequest;
import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.service.AdminStaffService;
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
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminStaffController {

  private final AdminStaffService adminStaffService;

  @GetMapping
  public ResponseEntity<?> getStaff() {
    try {
      List<AdminStaffResponse> response = adminStaffService.getStaff();
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping
  public ResponseEntity<?> createStaff(@RequestBody CreateAdminStaffRequest request) {
    try {
      AdminStaffResponse response = adminStaffService.createStaff(request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maNV}")
  public ResponseEntity<?> updateStaff(
          @PathVariable String maNV,
          @RequestBody CreateAdminStaffRequest request
  ) {
    try {
      AdminStaffResponse response = adminStaffService.updateStaff(maNV, request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @DeleteMapping("/{maNV}")
  public ResponseEntity<?> deleteStaff(@PathVariable String maNV) {
    try {
      adminStaffService.deleteStaff(maNV);
      return ResponseEntity.ok(new ApiResponse(true, "Xoa nhan vien thanh cong."));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }
}
