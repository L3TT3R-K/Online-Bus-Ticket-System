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

@RestController
@RequestMapping("/api/staff/xe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StaffXeController {

  private final StaffXeService staffXeService;


  @GetMapping
  public ResponseEntity<?> getXeCuaNhaXe(
      @RequestHeader("X-MaTK") Long maTK
  ) {
    try {
      return ResponseEntity.ok(
          staffXeService.getXeCuaNhaXe(maTK)
      );

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @GetMapping("/active")
  public ResponseEntity<?> getActiveXeCuaNhaXe(
      @RequestHeader("X-MaTK") Long maTK
  ) {
    try {
      return ResponseEntity.ok(
          staffXeService.getActiveXeCuaNhaXe(maTK)
      );

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping
  public ResponseEntity<?> createXe(
          @RequestHeader("X-MaTK") Long maTK,
          @RequestBody CreateStaffXeRequest request
  ) {
    try {
      StaffXeResponse response = staffXeService.createXe(maTK, request);

      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maXe}/status")
  public ResponseEntity<?> updateXeStatus(
          @PathVariable String maXe,
          @RequestHeader("X-MaTK") Long maTK,
          @RequestBody UpdateXeStatusRequest request
  ) {
    try {
      StaffXeResponse response = staffXeService.updateXeStatus(maTK, maXe, request);

      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maXe}")
  public ResponseEntity<?> updateXe(
          @PathVariable String maXe,
          @RequestHeader("X-MaTK") Long maTK,
          @RequestBody UpdateStaffXeRequest request
  ) {
    try {
      StaffXeResponse response = staffXeService.updateXe(maTK, maXe, request);

      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @DeleteMapping("/{maXe}")
  public ResponseEntity<?> deleteXe(
          @PathVariable String maXe,
          @RequestHeader("X-MaTK") Long maTK
  ) {
    try {
      staffXeService.deleteXe(maTK, maXe);

      return ResponseEntity.ok(new ApiResponse(true, "Xoa xe thanh cong"));

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

}
