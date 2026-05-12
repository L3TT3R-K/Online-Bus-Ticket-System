package com.busticket.api.service;

import com.busticket.api.dto.admin.AdminAccountResponse;
import com.busticket.api.dto.admin.CreateAdminAccountRequest;
import com.busticket.api.entity.KhachHang;
import com.busticket.api.entity.NhanVien;
import com.busticket.api.entity.NhaXe;
import com.busticket.api.entity.TaiKhoan;
import com.busticket.api.repository.KhachHangRepository;
import com.busticket.api.repository.NhanVienRepository;
import com.busticket.api.repository.NhaXeRepository;
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
public class AdminAccountService {

  private final TaiKhoanRepository taiKhoanRepository;
  private final KhachHangRepository khachHangRepository;
  private final NhanVienRepository nhanVienRepository;
  private final NhaXeRepository nhaXeRepository;
  private final PasswordEncoder passwordEncoder;

  public List<AdminAccountResponse> getAccounts() {
    return taiKhoanRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(TaiKhoan::getMaTK).reversed())
            .map(this::mapToResponse)
            .toList();
  }

  @Transactional
  public AdminAccountResponse createAccount(CreateAdminAccountRequest request) {
    if (request == null) {
      throw new RuntimeException("Dữ liệu tài khoản không được để trống.");
    }

    String tenDangNhap = requireText(
            request.getTenDangNhap(),
            "Tên đăng nhập không được để trống."
    );
    String matKhau = requirePassword(request.getMatKhau());
    String quyen = requireRole(request.getQuyen());
    String trangThaiTK = normalizeAccountStatus(request.getTrangThaiTK());

    if (taiKhoanRepository.existsByTenDangNhap(tenDangNhap)) {
      throw new RuntimeException("Tên đăng nhập đã tồn tại.");
    }

    TaiKhoan taiKhoan = TaiKhoan.builder()
            .tenDangNhap(tenDangNhap)
            .matKhau(passwordEncoder.encode(matKhau))
            .quyen(quyen)
            .trangThaiTK(trangThaiTK)
            .build();

    TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

    if ("KhachHang".equals(quyen)) {
      createCustomerProfile(savedTaiKhoan, request);
    } else if ("NhanVien".equals(quyen)) {
      createStaffProfile(savedTaiKhoan, request);
    }

    return mapToResponse(savedTaiKhoan);
  }

  private AdminAccountResponse mapToResponse(TaiKhoan taiKhoan) {
    KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);
    if (khachHang != null) {
      return new AdminAccountResponse(
              taiKhoan.getMaTK(),
              taiKhoan.getTenDangNhap(),
              taiKhoan.getQuyen(),
              taiKhoan.getTrangThaiTK(),
              taiKhoan.getNgayTao(),
              khachHang.getMaKH(),
              khachHang.getTenKH(),
              khachHang.getEmail(),
              khachHang.getSdt(),
              "KhachHang"
      );
    }

    NhanVien nhanVien = nhanVienRepository.findByTaiKhoan(taiKhoan).orElse(null);
    if (nhanVien != null) {
      return new AdminAccountResponse(
              taiKhoan.getMaTK(),
              taiKhoan.getTenDangNhap(),
              taiKhoan.getQuyen(),
              taiKhoan.getTrangThaiTK(),
              taiKhoan.getNgayTao(),
              nhanVien.getMaNV(),
              nhanVien.getTenNV(),
              nhanVien.getEmail(),
              nhanVien.getSdt(),
              "NhanVien"
      );
    }

    return new AdminAccountResponse(
            taiKhoan.getMaTK(),
            taiKhoan.getTenDangNhap(),
            taiKhoan.getQuyen(),
            taiKhoan.getTrangThaiTK(),
            taiKhoan.getNgayTao(),
            null,
            null,
            null,
            null,
            "TaiKhoan"
    );
  }

  private void createCustomerProfile(TaiKhoan taiKhoan, CreateAdminAccountRequest request) {
    String tenKhachHang = requireText(
            request.getTenNguoiDung(),
            "Tên khách hàng không được để trống."
    );

    validateCustomerContact(request.getEmail(), request.getSdt());

    KhachHang khachHang = KhachHang.builder()
            .maKH(generateCode("KH"))
            .tenKH(tenKhachHang)
            .ngaySinh(request.getNgaySinh())
            .gioiTinh(normalizeOptionalText(request.getGioiTinh()))
            .sdt(normalizeOptionalText(request.getSdt()))
            .email(normalizeOptionalText(request.getEmail()))
            .taiKhoan(taiKhoan)
            .trangThai("Hoạt động")
            .build();

    khachHangRepository.save(khachHang);
  }

  private void createStaffProfile(TaiKhoan taiKhoan, CreateAdminAccountRequest request) {
    String tenNhanVien = requireText(
            request.getTenNguoiDung(),
            "Tên nhân viên không được để trống."
    );
    String maNhaXe = requireText(
            request.getMaNhaXe(),
            "Mã nhà xe không được để trống."
    );

    NhaXe nhaXe = nhaXeRepository.findById(maNhaXe)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà xe."));

    validateStaffContact(request.getEmail(), request.getSdt());

    NhanVien nhanVien = new NhanVien();
    nhanVien.setMaNV(generateCode("NV"));
    nhanVien.setTenNV(tenNhanVien);
    nhanVien.setGioiTinh(normalizeOptionalText(request.getGioiTinh()));
    nhanVien.setSdt(normalizeOptionalText(request.getSdt()));
    nhanVien.setEmail(normalizeOptionalText(request.getEmail()));
    nhanVien.setNgayVaoLam(request.getNgayVaoLam() != null ? request.getNgayVaoLam() : LocalDate.now());
    nhanVien.setTrangThai("Hoạt động");
    nhanVien.setNhaXe(nhaXe);
    nhanVien.setTaiKhoan(taiKhoan);

    nhanVienRepository.save(nhanVien);
  }

  private void validateCustomerContact(String email, String sdt) {
    String normalizedEmail = normalizeOptionalText(email);
    if (normalizedEmail != null && khachHangRepository.existsByEmail(normalizedEmail)) {
      throw new RuntimeException("Email đã được sử dụng.");
    }

    String normalizedSdt = normalizeOptionalText(sdt);
    if (normalizedSdt != null && khachHangRepository.existsBySdt(normalizedSdt)) {
      throw new RuntimeException("Số điện thoại đã được sử dụng.");
    }
  }

  private void validateStaffContact(String email, String sdt) {
    String normalizedEmail = normalizeOptionalText(email);
    if (normalizedEmail != null && nhanVienRepository.existsByEmail(normalizedEmail)) {
      throw new RuntimeException("Email nhân viên đã được sử dụng.");
    }

    String normalizedSdt = normalizeOptionalText(sdt);
    if (normalizedSdt != null && nhanVienRepository.existsBySdt(normalizedSdt)) {
      throw new RuntimeException("Số điện thoại nhân viên đã được sử dụng.");
    }
  }

  private String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new RuntimeException(message);
    }

    return value.trim();
  }

  private String requirePassword(String value) {
    String password = requireText(value, "Mật khẩu không được để trống.");
    if (password.length() < 6) {
      throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự.");
    }

    return password;
  }

  private String requireRole(String value) {
    String role = requireText(value, "Quyền tài khoản không được để trống.");
    if (!"Admin".equals(role) && !"KhachHang".equals(role) && !"NhanVien".equals(role)) {
      throw new RuntimeException("Quyền tài khoản không hợp lệ.");
    }

    return role;
  }

  private String normalizeAccountStatus(String value) {
    if (value == null || value.isBlank()) {
      return "Hoạt động";
    }

    String status = value.trim();
    if (!"Hoạt động".equals(status) && !"Bị khóa".equals(status)) {
      throw new RuntimeException("Trạng thái tài khoản không hợp lệ.");
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
