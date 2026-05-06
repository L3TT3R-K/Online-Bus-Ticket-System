package com.busticket.api.controller;

import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.dto.staffxe.CreateStaffXeRequest;
import com.busticket.api.dto.staffxe.StaffXeResponse;
import com.busticket.api.dto.staffxe.UpdateStaffXeRequest;
import com.busticket.api.dto.staffxe.UpdateXeStatusRequest;
import com.busticket.api.service.StaffXeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
@RestController
@RequestMapping("/api/staff/xe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StaffXeController {

  private final StaffXeService staffXeService;


  @GetMapping
  public ResponseEntity<?> getXeCuaNhaXe(
          @RequestHeader(value = "X-MaTK", required = false) Long maTK,
          @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    try {
      Long currentMaTK = resolveMaTK(maTK, authorization);

      return ResponseEntity.ok(
              staffXeService.getXeCuaNhaXe(currentMaTK)
      );

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping
  public ResponseEntity<?> createXe(
          @RequestHeader(value = "X-MaTK", required = false) Long maTK,
          @RequestHeader(value = "Authorization", required = false) String authorization,
          @RequestBody CreateStaffXeRequest request
  ) {
    try {
      Long currentMaTK = resolveMaTK(maTK, authorization);

      StaffXeResponse response = staffXeService.createXe(currentMaTK, request);

      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maXe}/status")
  public ResponseEntity<?> updateXeStatus(
          @PathVariable String maXe,
          @RequestHeader(value = "X-MaTK", required = false) Long maTK,
          @RequestHeader(value = "Authorization", required = false) String authorization,
          @RequestBody UpdateXeStatusRequest request
  ) {
    try {
      Long currentMaTK = resolveMaTK(maTK, authorization);

      StaffXeResponse response = staffXeService.updateXeStatus(currentMaTK, maXe, request);

      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maXe}")
  public ResponseEntity<?> updateXe(
          @PathVariable String maXe,
          @RequestHeader(value = "X-MaTK", required = false) Long maTK,
          @RequestHeader(value = "Authorization", required = false) String authorization,
          @RequestBody UpdateStaffXeRequest request
  ) {
    try {
      Long currentMaTK = resolveMaTK(maTK, authorization);

      StaffXeResponse response = staffXeService.updateXe(currentMaTK, maXe, request);

      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  private Long resolveMaTK(Long maTK, String authorization) {
    Long currentMaTK = maTK;

    if (currentMaTK == null) {
      currentMaTK = extractMaTKFromDemoToken(authorization);
    }

    if (currentMaTK == null) {
      throw new RuntimeException("Thiếu mã tài khoản nhân viên");
    }

    return currentMaTK;
  }

  private Long extractMaTKFromDemoToken(String authorization) {
    if (authorization == null || authorization.isBlank()) {
      return null;
    }

    Pattern pattern = Pattern.compile("demo-token-(\\d+)");
    Matcher matcher = pattern.matcher(authorization);

    if (matcher.find()) {
      return Long.parseLong(matcher.group(1));
    }

    return null;
  }
}