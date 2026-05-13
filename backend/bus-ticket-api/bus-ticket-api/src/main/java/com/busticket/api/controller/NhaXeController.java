package com.busticket.api.controller;

import com.busticket.api.service.NhaXeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nha-xe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NhaXeController {

  private final NhaXeService nhaXeService;

  @GetMapping
  public ResponseEntity<?> getAllNhaXe() {
    return ResponseEntity.ok(nhaXeService.getAllNhaXe());
  }
}
