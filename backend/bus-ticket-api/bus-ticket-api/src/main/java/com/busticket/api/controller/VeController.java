package com.busticket.api.controller;

import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.dto.ve.KhachHangVeResponse;
import com.busticket.api.dto.ve.UpdateVeStatusRequest;
import com.busticket.api.service.VeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ve")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VeController {

  private final VeService veService;

  @GetMapping("/khach-hang/{maKhachHang}")
  public ResponseEntity<?> getVeByKhachHang(@PathVariable String maKhachHang) {
    try {
      List<KhachHangVeResponse> response = veService.getVeByKhachHang(maKhachHang);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PostMapping("/{maVe}/update-status")
  public ResponseEntity<?> updateVeStatus(
          @PathVariable String maVe,
          @RequestBody UpdateVeStatusRequest request
  ) {
    try {
      KhachHangVeResponse response = veService.updateStatus(maVe, request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
  }
}
