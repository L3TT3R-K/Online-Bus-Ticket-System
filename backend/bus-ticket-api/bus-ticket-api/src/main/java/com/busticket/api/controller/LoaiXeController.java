package com.busticket.api.controller;

import com.busticket.api.service.LoaiXeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loai-xe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoaiXeController {

  private final LoaiXeService loaiXeService;

  @GetMapping
  public ResponseEntity<?> getAllLoaiXe() {
    return ResponseEntity.ok(loaiXeService.getAllLoaiXe());
  }
}