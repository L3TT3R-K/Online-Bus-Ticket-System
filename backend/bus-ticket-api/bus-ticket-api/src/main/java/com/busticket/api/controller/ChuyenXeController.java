package com.busticket.api.controller;

import com.busticket.api.dto.chuyenxe.ChuyenXeSearchResponse;
import com.busticket.api.service.ChuyenXeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chuyen-xe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChuyenXeController {
  private final ChuyenXeService chuyenXeService;

  @GetMapping("/search")
  public ResponseEntity<?> searchChuyenXe(
          @RequestParam String diemDi,
          @RequestParam String diemDen,
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate ngayDi
  ) {
    try {
      List<ChuyenXeSearchResponse> data = chuyenXeService.searchChuyenXe(
              diemDen, diemDi, ngayDi
      );
      return ResponseEntity.ok(Map.of(
              "success", true,
              "data", data
      ));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(
              Map.of(
                      "success", false,
                      "message", e.getMessage()
              )
      );
    }
  }
}
