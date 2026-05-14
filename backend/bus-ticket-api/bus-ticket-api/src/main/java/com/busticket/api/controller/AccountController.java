package com.busticket.api.controller;

import com.busticket.api.dto.account.AccountResponse;
import com.busticket.api.dto.common.ApiResponse;
import com.busticket.api.dto.account.ChangePasswordRequest;
import com.busticket.api.dto.account.UpdateAccountRequest;
import com.busticket.api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AccountController {
  private final AccountService accountService;

  @GetMapping("/{maTK}")
  public ResponseEntity<?> getAccount(
          @PathVariable Long maTK,
          @RequestHeader("X-MaTK") Long authenticatedMaTK
  ) {
     try {
       validateAccountOwner(maTK, authenticatedMaTK);
       AccountResponse response = accountService.getAccount(maTK);
       return ResponseEntity.ok(response);
     }
     catch(RuntimeException e) {
       return ResponseEntity.badRequest().body(new ApiResponse(false,e.getMessage()));
     }
  }

  @PutMapping("/{maTK}")
  public ResponseEntity<?> updateAccount(
          @PathVariable Long maTK,
          @RequestHeader("X-MaTK") Long authenticatedMaTK,
          @RequestBody UpdateAccountRequest request
  ) {
    try {
      validateAccountOwner(maTK, authenticatedMaTK);
      AccountResponse response = accountService.updateAccount(maTK,request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maTK}/password")
  public ResponseEntity<?> changePassword(
          @PathVariable Long maTK,
          @RequestHeader("X-MaTK") Long authenticatedMaTK,
          @RequestBody ChangePasswordRequest request
  ) {
     try {
       validateAccountOwner(maTK, authenticatedMaTK);
       accountService.changePassword(maTK,request);
       return  ResponseEntity.ok(new ApiResponse(true, "Đổi mật khẩu thành công"));
     } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
     }
  }

  private void validateAccountOwner(Long maTK, Long authenticatedMaTK) {
    if (maTK == null || authenticatedMaTK == null || !maTK.equals(authenticatedMaTK)) {
      throw new RuntimeException("Bạn không có quyền truy cập tài khoản này.");
    }
  }
}
