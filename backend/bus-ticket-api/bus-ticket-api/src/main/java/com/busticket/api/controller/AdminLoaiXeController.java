package com.busticket.api.controller;

import com.busticket.api.dto.admin.CreateAdminLoaiXeRequest;
import com.busticket.api.dto.admin.UpdateAdminLoaiXeRequest;
import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.dto.loaixe.LoaiXeResponse;
import com.busticket.api.service.LoaiXeService;
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
@RequestMapping("/api/admin/loai-xe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminLoaiXeController {

  private final LoaiXeService loaiXeService;

  @GetMapping
  public ResponseEntity<?> getLoaiXe() {
    try {
      List<LoaiXeResponse> response = loaiXeService.getAllLoaiXe();
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping
  public ResponseEntity<?> createLoaiXe(@RequestBody CreateAdminLoaiXeRequest request) {
    try {
      LoaiXeResponse response = loaiXeService.createLoaiXe(request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maLoaiXe}")
  public ResponseEntity<?> updateLoaiXe(
          @PathVariable String maLoaiXe,
          @RequestBody UpdateAdminLoaiXeRequest request
  ) {
    try {
      LoaiXeResponse response = loaiXeService.updateLoaiXe(maLoaiXe, request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }
}
