package com.busticket.api.dto.chuyenxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ChuyenXeDiemDonTraResponse {

    private String maDiem;

    private String tenDiem;

    private Integer thuTu;

    private LocalDateTime thoiGian;
}
