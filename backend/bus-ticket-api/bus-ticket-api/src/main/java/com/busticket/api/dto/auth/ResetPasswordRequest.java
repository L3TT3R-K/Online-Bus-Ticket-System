package com.busticket.api.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    private String token;
    private String matKhauMoi;
    private String xacNhanMatKhauMoi;
}
