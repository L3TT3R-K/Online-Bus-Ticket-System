package com.busticket.api.controller;

import com.busticket.api.dto.danhgia.CreateDanhGiaRequest;
import com.busticket.api.dto.danhgia.CreateDanhGiaResponse;
import com.busticket.api.service.DanhGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/danh-gia")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DanhGiaController {

  private final DanhGiaService danhGiaService;

  @PostMapping
  public ResponseEntity<?> createDanhGia(@RequestBody CreateDanhGiaRequest request) {
    try {
      CreateDanhGiaResponse data = danhGiaService.create(request);

      return ResponseEntity.ok(Map.of(
              "success", true,
              "message", "Tạo đánh giá thành công.",
              "data", data
      ));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of(
              "success", false,
              "message", e.getMessage()
      ));
    }
  }
}
