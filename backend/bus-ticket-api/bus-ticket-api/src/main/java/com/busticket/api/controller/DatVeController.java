package com.busticket.api.controller;

import com.busticket.api.dto.datve.CreateDatVeRequest;
import com.busticket.api.dto.datve.CreateDatVeResponse;
import com.busticket.api.service.DatVeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dat-ve")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DatVeController {

  private final DatVeService datVeService;

  @PostMapping("/create")
  public ResponseEntity<?> createDatVe(@RequestBody CreateDatVeRequest request) {
    try {
      CreateDatVeResponse data = datVeService.create(request);

      return ResponseEntity.ok(Map.of(
              "success", true,
              "message", "Tạo đặt vé thành công.",
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
