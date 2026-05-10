package com.busticket.api.controller;

import com.busticket.api.dto.benxe.DiemBenResponse;
import com.busticket.api.dto.staffxe.BenXeResponse;
import com.busticket.api.service.BenXeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ben-xe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BenXeController {

  private final BenXeService benXeService;

  @GetMapping
  public List<BenXeResponse> getAllBenXe() {

    return benXeService.getAllBenXe();

  }

  @GetMapping("/{maBen}/diem-don-tra")
  public ResponseEntity<?> getDiemDonTraByBen(
          @PathVariable String maBen,
          @RequestParam(required = false) String loai
  ) {
    try {
      List<DiemBenResponse> data = benXeService.getDiemDonTraByBen(maBen, loai);
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
}
