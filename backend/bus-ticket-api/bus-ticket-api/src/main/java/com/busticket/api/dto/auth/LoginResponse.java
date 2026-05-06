package com.busticket.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String message;
    private Long maTK;
    private String tenDangNhap;
    private String quyen;
    private String tenKH;
    private String token;
}
