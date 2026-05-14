package com.busticket.api.controller;

import com.busticket.api.dto.admin.AdminKhuyenMaiResponse;
import com.busticket.api.dto.admin.CreateAdminKhuyenMaiRequest;
import com.busticket.api.dto.admin.UpdateAdminKhuyenMaiRequest;
import com.busticket.api.dto.admin.UpdateAdminKhuyenMaiStatusRequest;
import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.service.AdminKhuyenMaiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/khuyen-mai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminKhuyenMaiController {

  private final AdminKhuyenMaiService adminKhuyenMaiService;

  @GetMapping
  public ResponseEntity<?> getKhuyenMai() {
    try {
      List<AdminKhuyenMaiResponse> response = adminKhuyenMaiService.getKhuyenMai();
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @GetMapping("/{maKhuyenMai}")
  public ResponseEntity<?> getKhuyenMaiById(@PathVariable String maKhuyenMai) {
    try {
      AdminKhuyenMaiResponse response = adminKhuyenMaiService.getKhuyenMaiById(maKhuyenMai);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping
  public ResponseEntity<?> createKhuyenMai(@RequestBody CreateAdminKhuyenMaiRequest request) {
    try {
      AdminKhuyenMaiResponse response = adminKhuyenMaiService.createKhuyenMai(request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maKhuyenMai}")
  public ResponseEntity<?> updateKhuyenMai(
          @PathVariable String maKhuyenMai,
          @RequestBody UpdateAdminKhuyenMaiRequest request
  ) {
    try {
      AdminKhuyenMaiResponse response = adminKhuyenMaiService.updateKhuyenMai(maKhuyenMai, request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maKhuyenMai}/status")
  public ResponseEntity<?> updateKhuyenMaiStatus(
          @PathVariable String maKhuyenMai,
          @RequestBody UpdateAdminKhuyenMaiStatusRequest request
  ) {
    try {
      AdminKhuyenMaiResponse response = adminKhuyenMaiService.updateKhuyenMaiStatus(maKhuyenMai, request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }
}
