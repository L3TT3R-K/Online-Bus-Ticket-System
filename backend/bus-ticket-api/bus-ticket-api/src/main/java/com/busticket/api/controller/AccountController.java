package com.busticket.api.controller;

import com.busticket.api.dto.AccountResponse;
import com.busticket.api.dto.ApiResponse;
import com.busticket.api.dto.ChangePasswordRequest;
import com.busticket.api.dto.UpdateAccountRequest;
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
  public ResponseEntity<?> getAccount(@PathVariable Long maTK) {
     try {
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
          @RequestBody UpdateAccountRequest request
  ) {
    try {
      AccountResponse response = accountService.updateAccount(maTK,request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
              .body(new ApiResponse(false, e.getMessage()));
    }
  }

  @PutMapping("/{maTK}/password")
  public ResponseEntity<?> changePassword(@PathVariable Long maTK, @RequestBody ChangePasswordRequest request) {
     try {
       accountService.changePassword(maTK,request);
       return  ResponseEntity.ok(new ApiResponse(true, "Đổi mật khẩu thành công"));
     } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
     }
  }
}
