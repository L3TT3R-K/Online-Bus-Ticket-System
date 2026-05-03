package com.busticket.api.controller;

import com.busticket.api.dto.ApiResponse;
import com.busticket.api.dto.StaffMeResponse;
import com.busticket.api.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StaffController {

  private final StaffService staffService;

  @GetMapping("/me")
  public ResponseEntity<?> getCurrentStaff(
          @RequestHeader(value = "X-MaTK", required = false) Long maTK,
          @RequestHeader(value = "Authorization", required = false) String authorization
  ) {
    try {
      Long currentMaTK = maTK;

      if (currentMaTK == null) {
        currentMaTK = extractMaTKFromDemoToken(authorization);
      }

      if (currentMaTK == null) {
        throw new RuntimeException("Thiếu mã tài khoản nhân viên");
      }

      StaffMeResponse response = staffService.getCurrentStaff(currentMaTK);
      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
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