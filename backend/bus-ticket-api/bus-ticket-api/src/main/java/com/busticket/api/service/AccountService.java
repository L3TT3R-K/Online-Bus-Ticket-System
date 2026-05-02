package com.busticket.api.service;

import com.busticket.api.dto.AccountResponse;
import com.busticket.api.dto.ChangePasswordRequest;
import com.busticket.api.dto.UpdateAccountRequest;
import com.busticket.api.entity.KhachHang;
import com.busticket.api.entity.TaiKhoan;
import com.busticket.api.repository.KhachHangRepository;
import com.busticket.api.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final TaiKhoanRepository taiKhoanRepository;
  private final KhachHangRepository khachHangRepository;
  private final PasswordEncoder passwordEncoder;

  public AccountResponse getAccount(Long maTK) {
    TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

    KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));

    return buildAccountResponse(taiKhoan, khachHang, "Lấy thông tin tài khoản thành công");
  }

  public AccountResponse updateAccount(Long maTK, UpdateAccountRequest request) {
    TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

    KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));

    if (request.getTenKH() == null || request.getTenKH().trim().isEmpty()) {
      throw new RuntimeException("Họ tên không được để trống");
    }

    if (request.getSdt() == null || request.getSdt().trim().isEmpty()) {
      throw new RuntimeException("Số điện thoại không được để trống");
    }

    if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
      throw new RuntimeException("Email không được để trống");
    }

    khachHangRepository.findBySdt(request.getSdt().trim())
            .ifPresent(existing -> {
              if (!existing.getMaKH().equals(khachHang.getMaKH())) {
                throw new RuntimeException("Số điện thoại đã được sử dụng");
              }
            });

    khachHangRepository.findByEmail(request.getEmail().trim())
            .ifPresent(existing -> {
              if (!existing.getMaKH().equals(khachHang.getMaKH())) {
                throw new RuntimeException("Email đã được sử dụng");
              }
            });

    khachHang.setTenKH(request.getTenKH().trim());
    khachHang.setSdt(request.getSdt().trim());
    khachHang.setEmail(request.getEmail().trim());
    khachHang.setNgaySinh(request.getNgaySinh());
    khachHang.setGioiTinh(request.getGioiTinh());

    KhachHang savedKhachHang = khachHangRepository.save(khachHang);

    return buildAccountResponse(
            taiKhoan,
            savedKhachHang,
            "Cập nhật thông tin tài khoản thành công"
    );
  }

  public void changePassword(Long maTK, ChangePasswordRequest request) {
    TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

    if (request.getMatKhauCu() == null || request.getMatKhauCu().trim().isEmpty()) {
      throw new RuntimeException("Mật khẩu hiện tại không được để trống");
    }

    if (!passwordEncoder.matches(request.getMatKhauCu(), taiKhoan.getMatKhau())) {
      throw new RuntimeException("Mật khẩu hiện tại không đúng");
    }

    if (request.getMatKhauMoi() == null || request.getMatKhauMoi().trim().isEmpty()) {
      throw new RuntimeException("Mật khẩu mới không được để trống");
    }

    if (request.getMatKhauMoi().length() < 6) {
      throw new RuntimeException("Mật khẩu mới phải có ít nhất 6 ký tự");
    }

    if (!request.getMatKhauMoi().equals(request.getXacNhanMatKhauMoi())) {
      throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không khớp");
    }

    taiKhoan.setMatKhau(passwordEncoder.encode(request.getMatKhauMoi()));

    taiKhoanRepository.save(taiKhoan);
  }

  private AccountResponse buildAccountResponse(
          TaiKhoan taiKhoan,
          KhachHang khachHang,
          String message
  ) {
    return new AccountResponse(
            true,
            message,
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