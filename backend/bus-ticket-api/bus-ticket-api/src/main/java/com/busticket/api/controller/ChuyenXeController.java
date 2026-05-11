package com.busticket.api.controller;

import com.busticket.api.dto.chuyenxe.ChuyenXeDiemDonTraListResponse;
import com.busticket.api.dto.chuyenxe.ChuyenXeSaveDiemDonTraRequest;
import com.busticket.api.dto.chuyenxe.ChuyenXeSearchResponse;
import com.busticket.api.dto.chuyenxe.ChuyenXeSeatResponse;
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


  @GetMapping("/{maChuyen}")
  public ResponseEntity<?> getChuyenXeByMaChuyen(
          @PathVariable String maChuyen
  ) {
    try {
      ChuyenXeSearchResponse data = chuyenXeService.getChuyenXeByMaChuyen(maChuyen);
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

  @GetMapping("/{maChuyen}/ghe")
  public List<ChuyenXeSeatResponse> getSeatMapByTrip(
          @PathVariable String maChuyen
  ) {
    return chuyenXeService.getSeatMapByTrip(maChuyen);
  }

  @GetMapping("/{maChuyen}/diem-don-tra")
  public ResponseEntity<?> getDiemDonTraByTrip(
          @PathVariable String maChuyen
  ) {
    try {
      ChuyenXeDiemDonTraListResponse data = chuyenXeService.getDiemDonTraByTrip(maChuyen);
      return ResponseEntity.ok(Map.of(
              "success", true,
              "data", data
      ));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of(
              "success", false,
              "message", e.getMessage()
      ));
    }
  }

  @PostMapping("/{maChuyen}/diem-don-tra")
  public ResponseEntity<?> saveDiemDonTraByTrip(
          @PathVariable String maChuyen,
          @RequestBody ChuyenXeSaveDiemDonTraRequest request
  ) {
    try {
      ChuyenXeDiemDonTraListResponse data = chuyenXeService.saveDiemDonTraByTrip(maChuyen, request);
      return ResponseEntity.ok(Map.of(
              "success", true,
              "message", "Lưu điểm đón trả thành công.",
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
