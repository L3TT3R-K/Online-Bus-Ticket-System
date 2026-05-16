package com.busticket.api.controller;

import com.busticket.api.dto.admin.AdminBenXeRequest;
import com.busticket.api.dto.admin.AdminBenXeResponse;
import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.service.AdminBenXeService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ben-xe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminBenXeController {

  private final AdminBenXeService adminBenXeService;

  @GetMapping
  public ResponseEntity<?> getBenXe(@RequestParam(required = false) String keyword) {
    try {
      List<AdminBenXeResponse> response = adminBenXeService.getBenXe(keyword);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping
  public ResponseEntity<?> createBenXe(@RequestBody AdminBenXeRequest request) {
    try {
      AdminBenXeResponse response = adminBenXeService.createBenXe(request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maBen}")
  public ResponseEntity<?> updateBenXe(
          @PathVariable String maBen,
          @RequestBody AdminBenXeRequest request
  ) {
    try {
      AdminBenXeResponse response = adminBenXeService.updateBenXe(maBen, request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @DeleteMapping("/{maBen}")
  public ResponseEntity<?> deleteBenXe(@PathVariable String maBen) {
    try {
      adminBenXeService.deleteBenXe(maBen);
      return ResponseEntity.ok(new ApiResponse(true, "Xoa ben xe thanh cong."));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }
}
