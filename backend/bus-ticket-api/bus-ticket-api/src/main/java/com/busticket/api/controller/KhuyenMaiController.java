package com.busticket.api.controller;

import com.busticket.api.service.KhuyenMaiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/khuyen-mai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KhuyenMaiController {

  private final KhuyenMaiService khuyenMaiService;

  @GetMapping("/active")
  public ResponseEntity<?> getActiveKhuyenMai() {
    return ResponseEntity.ok(khuyenMaiService.getActiveKhuyenMai());
  }
}
