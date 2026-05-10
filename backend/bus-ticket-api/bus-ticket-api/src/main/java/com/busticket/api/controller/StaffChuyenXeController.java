package com.busticket.api.controller;

import com.busticket.api.dto.staffxe.*;
import com.busticket.api.service.StaffChuyenXeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/chuyen-xe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StaffChuyenXeController {

  private final StaffChuyenXeService staffChuyenXeService;

  @GetMapping
  public List<StaffChuyenXeResponse> getStaffChuyenXe(
          @RequestHeader("X-MaTK") Integer maTK
  ) {
    return staffChuyenXeService.getChuyenXeByStaff(maTK);
  }
  @PostMapping
  public StaffChuyenXeResponse createChuyenXe(
          @RequestHeader("X-MaTK") Integer maTK,
          @RequestBody StaffCreateChuyenXeRequest request
  ) {
    return staffChuyenXeService.createChuyenXe(maTK, request);
  }

  @PutMapping("/{maChuyen}")
  public StaffChuyenXeResponse updateChuyenXe(
          @RequestHeader("X-MaTK") Integer maTK,
          @PathVariable String maChuyen,
          @RequestBody StaffUpdateChuyenXeRequest request
  ) {
    return staffChuyenXeService.updateChuyenXe(maTK, maChuyen, request);
  }

  @DeleteMapping("/{maChuyen}")
  public void deleteChuyenXe(
          @RequestHeader("X-MaTK") Integer maTK,
          @PathVariable String maChuyen
  ) {
    staffChuyenXeService.deleteChuyenXe(maTK, maChuyen);
  }

  @PutMapping("/{maChuyen}/status")
  public StaffChuyenXeResponse updateChuyenXeStatus(
          @RequestHeader("X-MaTK") Integer maTK,
          @PathVariable String maChuyen,
          @RequestBody StaffUpdateChuyenXeStatusRequest request
  ) {
    return staffChuyenXeService.updateChuyenXeStatus(
            maTK,
            maChuyen,
            request.getTrangThai()
    );
  }
  @GetMapping("/{maChuyen}/ghe")
  public List<StaffSeatMapResponse> getSeatMapByTrip(
          @RequestHeader("X-MaTK") Integer maTK,
          @PathVariable String maChuyen
  ) {
    return staffChuyenXeService.getSeatMapByTrip(maTK, maChuyen);
  }


}
