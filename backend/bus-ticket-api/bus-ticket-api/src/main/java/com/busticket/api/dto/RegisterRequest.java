package com.busticket.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String tenDangNhap;
    private String matKhau;
    private String xacNhanMatKhau;

    private String tenKH;
    private String sdt;
    private String email;
}