package com.busticket.api.service;

import com.busticket.api.dto.RegisterRequest;
import com.busticket.api.entity.KhachHang;
import com.busticket.api.entity.TaiKhoan;
import com.busticket.api.repository.KhachHangRepository;
import com.busticket.api.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {

        if (request.getTenDangNhap() == null || request.getTenDangNhap().trim().isEmpty()) {
            throw new RuntimeException("Tên đăng nhập không được để trống");
        }

        if (request.getMatKhau() == null || request.getMatKhau().trim().isEmpty()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }

        if (!request.getMatKhau().equals(request.getXacNhanMatKhau())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        if (request.getTenKH() == null || request.getTenKH().trim().isEmpty()) {
            throw new RuntimeException("Tên khách hàng không được để trống");
        }

        if (taiKhoanRepository.existsByTenDangNhap(request.getTenDangNhap())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()
                && khachHangRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        if (request.getSdt() != null && !request.getSdt().trim().isEmpty()
                && khachHangRepository.existsBySdt(request.getSdt())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng");
        }

        TaiKhoan taiKhoan = TaiKhoan.builder()
                .tenDangNhap(request.getTenDangNhap())
                .matKhau(passwordEncoder.encode(request.getMatKhau()))
                .quyen("KhachHang")
                .trangThaiTK("Hoạt động")
                .build();

        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

        KhachHang khachHang = KhachHang.builder()
                .maKH(generateMaKH())
                .tenKH(request.getTenKH())
                .sdt(request.getSdt())
                .email(request.getEmail())
                .taiKhoan(savedTaiKhoan)
                .trangThai("Hoạt động")
                .build();

        khachHangRepository.save(khachHang);

        return "Đăng ký tài khoản thành công";
    }

    private String generateMaKH() {
        return "KH" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}