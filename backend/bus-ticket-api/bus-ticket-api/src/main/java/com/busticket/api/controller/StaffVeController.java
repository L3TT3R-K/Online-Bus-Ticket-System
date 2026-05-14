package com.busticket.api.controller;

import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.dto.staffxe.StaffVeResponse;
import com.busticket.api.service.StaffVeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/ve")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StaffVeController {

  private final StaffVeService staffVeService;

  @GetMapping
  public ResponseEntity<?> getVeCuaNhaXe(
          @RequestHeader("X-MaTK") Long maTK
  ) {
    try {
      List<StaffVeResponse> response = staffVeService.getVeCuaNhaXe(maTK);
      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

}
