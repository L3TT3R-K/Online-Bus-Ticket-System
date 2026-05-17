package com.busticket.api.service;

import com.busticket.api.dto.admin.AdminCustomerResponse;
import com.busticket.api.dto.admin.CreateAdminCustomerRequest;
import com.busticket.api.entity.KhachHang;
import com.busticket.api.entity.TaiKhoan;
import com.busticket.api.repository.KhachHangRepository;
import com.busticket.api.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminCustomerService {

  private final KhachHangRepository khachHangRepository;
  private final TaiKhoanRepository taiKhoanRepository;
  private final PasswordEncoder passwordEncoder;

  public List<AdminCustomerResponse> getCustomers() {
    return khachHangRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(KhachHang::getMaKH).reversed())
            .map(this::mapToResponse)
            .toList();
  }

  @Transactional
  public AdminCustomerResponse createCustomer(CreateAdminCustomerRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu khach hang khong duoc de trong.");
    }

    String tenDangNhap = requireText(request.getTenDangNhap(), "Ten dang nhap khong duoc de trong.");
    String matKhau = requirePassword(request.getMatKhau());
    String tenKH = requireText(request.getTenKH(), "Ten khach hang khong duoc de trong.");

    if (taiKhoanRepository.existsByTenDangNhap(tenDangNhap)) {
      throw new RuntimeException("Ten dang nhap da ton tai.");
    }

    validateCustomerContact(request.getEmail(), request.getSdt(), null);

    TaiKhoan taiKhoan = TaiKhoan.builder()
            .tenDangNhap(tenDangNhap)
            .matKhau(passwordEncoder.encode(matKhau))
            .quyen("KhachHang")
            .trangThaiTK(normalizeAccountStatus(request.getTrangThaiTK()))
            .build();

    KhachHang khachHang = KhachHang.builder()
            .maKH(generateCode("KH"))
            .tenKH(tenKH)
            .ngaySinh(request.getNgaySinh())
            .gioiTinh(normalizeOptionalText(request.getGioiTinh()))
            .sdt(normalizeOptionalText(request.getSdt()))
            .email(normalizeOptionalText(request.getEmail()))
            .trangThai(normalizeCustomerStatus(request.getTrangThai()))
            .taiKhoan(taiKhoanRepository.save(taiKhoan))
            .build();

    return mapToResponse(khachHangRepository.save(khachHang));
  }

  @Transactional
  public AdminCustomerResponse updateCustomer(String maKH, CreateAdminCustomerRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu khach hang khong duoc de trong.");
    }

    KhachHang khachHang = khachHangRepository.findById(requireText(maKH, "Ma khach hang khong duoc de trong."))
            .orElseThrow(() -> new RuntimeException("Khong tim thay khach hang."));
    TaiKhoan taiKhoan = khachHang.getTaiKhoan();

    String tenDangNhap = requireText(request.getTenDangNhap(), "Ten dang nhap khong duoc de trong.");
    taiKhoanRepository.findByTenDangNhap(tenDangNhap)
            .filter(existing -> !existing.getMaTK().equals(taiKhoan.getMaTK()))
            .ifPresent(existing -> {
              throw new RuntimeException("Ten dang nhap da ton tai.");
            });

    validateCustomerContact(request.getEmail(), request.getSdt(), khachHang.getMaKH());

    taiKhoan.setTenDangNhap(tenDangNhap);
    if (request.getMatKhau() != null && !request.getMatKhau().isBlank()) {
      taiKhoan.setMatKhau(passwordEncoder.encode(requirePassword(request.getMatKhau())));
    }
    taiKhoan.setQuyen("KhachHang");
    taiKhoan.setTrangThaiTK(normalizeAccountStatus(request.getTrangThaiTK()));

    khachHang.setTenKH(requireText(request.getTenKH(), "Ten khach hang khong duoc de trong."));
    khachHang.setNgaySinh(request.getNgaySinh());
    khachHang.setGioiTinh(normalizeOptionalText(request.getGioiTinh()));
    khachHang.setSdt(normalizeOptionalText(request.getSdt()));
    khachHang.setEmail(normalizeOptionalText(request.getEmail()));
    khachHang.setTrangThai(normalizeCustomerStatus(request.getTrangThai()));
    khachHang.setTaiKhoan(taiKhoanRepository.save(taiKhoan));

    return mapToResponse(khachHangRepository.save(khachHang));
  }

  @Transactional
  public void deleteCustomer(String maKH) {
    KhachHang khachHang = khachHangRepository.findById(requireText(maKH, "Ma khach hang khong duoc de trong."))
            .orElseThrow(() -> new RuntimeException("Khong tim thay khach hang."));
    TaiKhoan taiKhoan = khachHang.getTaiKhoan();

    khachHangRepository.delete(khachHang);
    if (taiKhoan != null) {
      taiKhoanRepository.delete(taiKhoan);
    }
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

  private void validateCustomerContact(String email, String sdt, String currentMaKH) {
    String normalizedEmail = normalizeOptionalText(email);
    if (normalizedEmail != null) {
      khachHangRepository.findByEmail(normalizedEmail)
              .filter(existing -> currentMaKH == null || !existing.getMaKH().equals(currentMaKH))
              .ifPresent(existing -> {
                throw new RuntimeException("Email khach hang da duoc su dung.");
              });
    }

    String normalizedSdt = normalizeOptionalText(sdt);
    if (normalizedSdt != null) {
      khachHangRepository.findBySdt(normalizedSdt)
              .filter(existing -> currentMaKH == null || !existing.getMaKH().equals(currentMaKH))
              .ifPresent(existing -> {
                throw new RuntimeException("So dien thoai khach hang da duoc su dung.");
              });
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
    if (password.length() < 1) {
      throw new RuntimeException("Mat khau khong duoc de trong.");
    }

    return password;
  }

  private String normalizeAccountStatus(String value) {
    if (value == null || value.isBlank()) {
      return "Hoạt động";
    }

    String status = value.trim();
    if (!"Hoạt động".equals(status) && !"Bị khóa".equals(status) && !"Chưa xác minh".equals(status)) {
      throw new RuntimeException("Trang thai tai khoan khong hop le.");
    }

    return status;
  }

  private String normalizeCustomerStatus(String value) {
    if (value == null || value.isBlank()) {
      return "Hoạt động";
    }

    String status = value.trim();
    if (!"Hoạt động".equals(status) && !"Bị khóa".equals(status) && !"Chưa xác minh".equals(status)) {
      throw new RuntimeException("Trang thai khach hang khong hop le.");
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
