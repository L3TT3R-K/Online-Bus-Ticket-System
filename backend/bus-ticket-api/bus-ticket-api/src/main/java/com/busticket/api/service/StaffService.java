package com.busticket.api.service;

import com.busticket.api.dto.staff.StaffMeResponse;
import com.busticket.api.entity.NhanVien;
import com.busticket.api.entity.TaiKhoan;
import com.busticket.api.repository.NhanVienRepository;
import com.busticket.api.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffService {

  private final TaiKhoanRepository taiKhoanRepository;
  private final NhanVienRepository nhanVienRepository;

  public StaffMeResponse getCurrentStaff(Long maTK) {
    TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

    if (!RoleUtils.isStaffRole(taiKhoan.getQuyen())) {
      throw new RuntimeException("Tài khoản này không phải nhân viên nhà xe");
    }

    if (!"Hoạt động".equals(taiKhoan.getTrangThaiTK())) {
      throw new RuntimeException("Tài khoản đã bị khóa");
    }

    NhanVien nhanVien = nhanVienRepository.findByTaiKhoan(taiKhoan)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));

    return new StaffMeResponse(
            true,
            "Lấy thông tin nhân viên thành công",
            taiKhoan.getMaTK(),
            taiKhoan.getTenDangNhap(),
            taiKhoan.getQuyen(),
            nhanVien.getMaNV(),
            nhanVien.getTenNV(),
            nhanVien.getSdt(),
            nhanVien.getEmail(),
            nhanVien.getTrangThai(),
            nhanVien.getNhaXe().getMaNhaXe(),
            nhanVien.getNhaXe().getTenNhaXe()
    );
  }
}
