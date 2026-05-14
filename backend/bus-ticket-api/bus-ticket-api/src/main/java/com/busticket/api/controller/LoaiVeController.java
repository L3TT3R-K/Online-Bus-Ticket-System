package com.busticket.api.controller;

import com.busticket.api.service.LoaiVeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loai-ve")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoaiVeController {

  private final LoaiVeService loaiVeService;

  @GetMapping
  public ResponseEntity<?> getAllLoaiVe() {
    return ResponseEntity.ok(loaiVeService.getAllLoaiVe());
  }

  @GetMapping("/active")
  public ResponseEntity<?> getActiveLoaiVe() {
    return ResponseEntity.ok(loaiVeService.getActiveLoaiVe());
  }
}
