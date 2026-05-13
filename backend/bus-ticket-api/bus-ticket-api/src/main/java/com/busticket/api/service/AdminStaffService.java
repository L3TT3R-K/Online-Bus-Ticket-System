package com.busticket.api.service;

import com.busticket.api.dto.admin.AdminStaffResponse;
import com.busticket.api.dto.admin.CreateAdminStaffRequest;
import com.busticket.api.entity.NhaXe;
import com.busticket.api.entity.NhanVien;
import com.busticket.api.entity.TaiKhoan;
import com.busticket.api.repository.NhaXeRepository;
import com.busticket.api.repository.NhanVienRepository;
import com.busticket.api.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminStaffService {

  private final NhanVienRepository nhanVienRepository;
  private final NhaXeRepository nhaXeRepository;
  private final TaiKhoanRepository taiKhoanRepository;
  private final PasswordEncoder passwordEncoder;

  public List<AdminStaffResponse> getStaff() {
    return nhanVienRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(NhanVien::getMaNV).reversed())
            .map(this::mapToResponse)
            .toList();
  }

  @Transactional
  public AdminStaffResponse createStaff(CreateAdminStaffRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu nhan vien khong duoc de trong.");
    }

    String tenDangNhap = requireText(request.getTenDangNhap(), "Ten dang nhap khong duoc de trong.");
    String matKhau = requirePassword(request.getMatKhau());
    String tenNV = requireText(request.getTenNV(), "Ten nhan vien khong duoc de trong.");
    String maNhaXe = requireText(request.getMaNhaXe(), "Ma nha xe khong duoc de trong.");
    String trangThaiTK = normalizeAccountStatus(request.getTrangThaiTK());
    String trangThai = normalizeStaffStatus(request.getTrangThai());

    if (taiKhoanRepository.existsByTenDangNhap(tenDangNhap)) {
      throw new RuntimeException("Ten dang nhap da ton tai.");
    }

    validateStaffContact(request.getEmail(), request.getSdt());

    NhaXe nhaXe = nhaXeRepository.findById(maNhaXe)
            .orElseThrow(() -> new RuntimeException("Khong tim thay nha xe."));

    TaiKhoan taiKhoan = TaiKhoan.builder()
            .tenDangNhap(tenDangNhap)
            .matKhau(passwordEncoder.encode(matKhau))
            .quyen("NhanVien")
            .trangThaiTK(trangThaiTK)
            .build();
    TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

    NhanVien nhanVien = new NhanVien();
    nhanVien.setMaNV(generateCode("NV"));
    nhanVien.setTenNV(tenNV);
    nhanVien.setGioiTinh(normalizeOptionalText(request.getGioiTinh()));
    nhanVien.setSdt(normalizeOptionalText(request.getSdt()));
    nhanVien.setEmail(normalizeOptionalText(request.getEmail()));
    nhanVien.setNgayVaoLam(request.getNgayVaoLam() != null ? request.getNgayVaoLam() : LocalDate.now());
    nhanVien.setTrangThai(trangThai);
    nhanVien.setNhaXe(nhaXe);
    nhanVien.setTaiKhoan(savedTaiKhoan);

    return mapToResponse(nhanVienRepository.save(nhanVien));
  }

  private AdminStaffResponse mapToResponse(NhanVien nhanVien) {
    NhaXe nhaXe = nhanVien.getNhaXe();
    TaiKhoan taiKhoan = nhanVien.getTaiKhoan();

    return new AdminStaffResponse(
            nhanVien.getMaNV(),
            nhanVien.getTenNV(),
            nhanVien.getGioiTinh(),
            nhanVien.getSdt(),
            nhanVien.getEmail(),
            nhanVien.getNgayVaoLam(),
            nhanVien.getTrangThai(),
            nhaXe != null ? nhaXe.getMaNhaXe() : null,
            nhaXe != null ? nhaXe.getTenNhaXe() : null,
            taiKhoan != null ? taiKhoan.getMaTK() : null,
            taiKhoan != null ? taiKhoan.getTenDangNhap() : null,
            taiKhoan != null ? taiKhoan.getTrangThaiTK() : null,
            taiKhoan != null ? taiKhoan.getNgayTao() : null
    );
  }

  private void validateStaffContact(String email, String sdt) {
    String normalizedEmail = normalizeOptionalText(email);
    if (normalizedEmail != null && nhanVienRepository.existsByEmail(normalizedEmail)) {
      throw new RuntimeException("Email nhan vien da duoc su dung.");
    }

    String normalizedSdt = normalizeOptionalText(sdt);
    if (normalizedSdt != null && nhanVienRepository.existsBySdt(normalizedSdt)) {
      throw new RuntimeException("So dien thoai nhan vien da duoc su dung.");
    }
  }

  private String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new RuntimeException(message);
    }

    return value.trim();
  }

  private String requirePassword(String value) {
    String password = requireText(value, "Mat khau khong duoc de trong.");
    if (password.length() < 6) {
      throw new RuntimeException("Mat khau phai co it nhat 6 ky tu.");
    }

    return password;
  }

  private String normalizeAccountStatus(String value) {
    if (value == null || value.isBlank()) {
      return "Hoạt động";
    }

    String status = value.trim();
    if (!"Hoạt động".equals(status) && !"Bị khóa".equals(status)) {
      throw new RuntimeException("Trang thai tai khoan khong hop le.");
    }

    return status;
  }

  private String normalizeStaffStatus(String value) {
    if (value == null || value.isBlank()) {
      return "Hoạt động";
    }

    String status = value.trim();
    if (!"Hoạt động".equals(status) && !"Tạm nghỉ".equals(status) && !"Nghỉ việc".equals(status)) {
      throw new RuntimeException("Trang thai nhan vien khong hop le.");
    }

    return status;
  }

  private String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private String generateCode(String prefix) {
    return prefix + UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 8)
            .toUpperCase();
  }
}
