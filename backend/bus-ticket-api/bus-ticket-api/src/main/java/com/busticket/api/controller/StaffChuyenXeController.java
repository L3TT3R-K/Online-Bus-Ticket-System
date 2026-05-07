package com.busticket.api.controller;

import com.busticket.api.dto.staffxe.StaffChuyenXeResponse;
import com.busticket.api.dto.staffxe.StaffCreateChuyenXeRequest;
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
}