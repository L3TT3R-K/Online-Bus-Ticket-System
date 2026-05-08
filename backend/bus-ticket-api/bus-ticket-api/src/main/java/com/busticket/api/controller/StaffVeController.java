package com.busticket.api.controller;

import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.dto.staffxe.StaffVeResponse;
import com.busticket.api.service.StaffVeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/staff/ve")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StaffVeController {

  private final StaffVeService staffVeService;

  @GetMapping
  public ResponseEntity<?> getVeCuaNhaXe(
          @RequestHeader(value = "X-MaTK", required = false) Long maTK,
          @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    try {
      Long currentMaTK = resolveMaTK(maTK, authorization);

      List<StaffVeResponse> response = staffVeService.getVeCuaNhaXe(currentMaTK);
      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  private Long resolveMaTK(Long maTK, String authorization) {
    Long currentMaTK = maTK;

    if (currentMaTK == null) {
      currentMaTK = extractMaTKFromDemoToken(authorization);
    }

    if (currentMaTK == null) {
      throw new RuntimeException("Thieu ma tai khoan nhan vien");
    }

    return currentMaTK;
  }

  private Long extractMaTKFromDemoToken(String authorization) {
    if (authorization == null || authorization.isBlank()) {
      return null;
    }

    Pattern pattern = Pattern.compile("demo-token-(\\d+)");
    Matcher matcher = pattern.matcher(authorization);

    if (matcher.find()) {
      return Long.parseLong(matcher.group(1));
    }

    return null;
  }
}
