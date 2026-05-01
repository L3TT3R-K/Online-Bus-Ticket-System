package com.busticket.api.service;

import com.busticket.api.entity.KhachHang;
import com.busticket.api.entity.TaiKhoan;
import com.busticket.api.dto.AccountResponse;
import com.busticket.api.repository.KhachHangRepository;
import com.busticket.api.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {
  private final TaiKhoanRepository taiKhoanRepository;
  private final KhachHangRepository khachHangRepository;

  public AccountResponse getAccount(Long maTK) {
    TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

    KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));

    return new AccountResponse(
            true,
            "Lấy thông tin tài khoản thành công",
            taiKhoan.getMaTK(),
            taiKhoan.getTenDangNhap(),
            taiKhoan.getQuyen(),
            taiKhoan.getTrangThaiTK(),
            khachHang.getMaKH(),
            khachHang.getTenKH(),
            khachHang.getNgaySinh(),
            khachHang.getGioiTinh(),
            khachHang.getSdt(),
            khachHang.getEmail(),
            khachHang.getTrangThai()
    );
  }
}
