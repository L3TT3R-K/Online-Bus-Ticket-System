package com.busticket.api.service;

import com.busticket.api.dto.admin.AdminCustomerResponse;
import com.busticket.api.entity.KhachHang;
import com.busticket.api.entity.TaiKhoan;
import com.busticket.api.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCustomerService {

  private final KhachHangRepository khachHangRepository;

  public List<AdminCustomerResponse> getCustomers() {
    return khachHangRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(KhachHang::getMaKH).reversed())
            .map(this::mapToResponse)
            .toList();
  }

  private AdminCustomerResponse mapToResponse(KhachHang khachHang) {
    TaiKhoan taiKhoan = khachHang.getTaiKhoan();

    return new AdminCustomerResponse(
            khachHang.getMaKH(),
            khachHang.getTenKH(),
            khachHang.getNgaySinh(),
            khachHang.getGioiTinh(),
            khachHang.getSdt(),
            khachHang.getEmail(),
            khachHang.getTrangThai(),
            taiKhoan != null ? taiKhoan.getMaTK() : null,
            taiKhoan != null ? taiKhoan.getTenDangNhap() : null,
            taiKhoan != null ? taiKhoan.getTrangThaiTK() : null,
            taiKhoan != null ? taiKhoan.getNgayTao() : null
    );
  }
}
