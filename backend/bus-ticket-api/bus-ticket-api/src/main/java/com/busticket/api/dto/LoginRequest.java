package com.busticket.api.dto;


import lombok.Data;

@Data
public class LoginRequest {
    private String tenDangNhap;
    private String matKhau;
}
