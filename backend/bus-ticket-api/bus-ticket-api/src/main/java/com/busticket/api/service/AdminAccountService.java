package com.busticket.api.service;

import com.busticket.api.dto.admin.AdminAccountResponse;
import com.busticket.api.dto.admin.CreateAdminAccountRequest;
import com.busticket.api.dto.admin.UpdateAdminAccountRequest;
import com.busticket.api.dto.admin.UpdateAdminAccountStatusRequest;
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

  @Transactional
  public AdminAccountResponse updateAccount(Long maTK, UpdateAdminAccountRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu tai khoan khong duoc de trong.");
    }

    TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
            .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan."));

    String tenDangNhap = normalizeOptionalText(request.getTenDangNhap());
    if (tenDangNhap != null) {
      taiKhoanRepository.findByTenDangNhap(tenDangNhap)
              .filter(existing -> !existing.getMaTK().equals(maTK))
              .ifPresent(existing -> {
                throw new RuntimeException("Ten dang nhap da ton tai.");
              });
      taiKhoan.setTenDangNhap(tenDangNhap);
    }

    if (request.getMatKhau() != null && !request.getMatKhau().isBlank()) {
      taiKhoan.setMatKhau(passwordEncoder.encode(requirePassword(request.getMatKhau())));
    }

    String quyen = normalizeOptionalText(request.getQuyen());
    if (quyen != null) {
      taiKhoan.setQuyen(requireRole(quyen));
    }

    if (request.getTrangThaiTK() != null && !request.getTrangThaiTK().isBlank()) {
      taiKhoan.setTrangThaiTK(normalizeAccountStatus(request.getTrangThaiTK()));
    }

    TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);
    if ("KhachHang".equals(savedTaiKhoan.getQuyen())) {
      updateCustomerProfile(savedTaiKhoan, request);
    } else if ("NhanVien".equals(savedTaiKhoan.getQuyen())) {
      updateStaffProfile(savedTaiKhoan, request);
    }

    return mapToResponse(savedTaiKhoan);
  }

  @Transactional
  public AdminAccountResponse updateAccountStatus(Long maTK, UpdateAdminAccountStatusRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu trang thai khong duoc de trong.");
    }

    String statusValue = request.getTrangThaiTK();
    if (statusValue == null || statusValue.isBlank()) {
      statusValue = request.getTrangThai();
    }

    String trangThaiTK = normalizeAccountStatus(statusValue);
    TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
            .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan."));

    taiKhoan.setTrangThaiTK(trangThaiTK);
    TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);
    syncProfileStatus(savedTaiKhoan, trangThaiTK);

    return mapToResponse(savedTaiKhoan);
  }

  @Transactional
  public void deleteAccount(Long maTK) {
    TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
            .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan."));

    khachHangRepository.findByTaiKhoan(taiKhoan).ifPresent(khachHangRepository::delete);
    nhanVienRepository.findByTaiKhoan(taiKhoan).ifPresent(nhanVienRepository::delete);
    taiKhoanRepository.delete(taiKhoan);
  }

  private AdminAccountResponse mapToResponse(TaiKhoan taiKhoan) {
    if ("KhachHang".equals(taiKhoan.getQuyen())) {
      KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);
      if (khachHang != null) {
        return mapCustomerResponse(taiKhoan, khachHang);
      }
    }

    if ("NhanVien".equals(taiKhoan.getQuyen())) {
      NhanVien nhanVien = nhanVienRepository.findByTaiKhoan(taiKhoan).orElse(null);
      if (nhanVien != null) {
        return mapStaffResponse(taiKhoan, nhanVien);
      }
    }

    KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);
    if (khachHang != null) {
      return mapCustomerResponse(taiKhoan, khachHang);
    }

    NhanVien nhanVien = nhanVienRepository.findByTaiKhoan(taiKhoan).orElse(null);
    if (nhanVien != null) {
      return mapStaffResponse(taiKhoan, nhanVien);
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
            "TaiKhoan",
            null,
            null
    );
  }

  private AdminAccountResponse mapCustomerResponse(TaiKhoan taiKhoan, KhachHang khachHang) {
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
            "KhachHang",
            null,
            null
    );
  }

  private AdminAccountResponse mapStaffResponse(TaiKhoan taiKhoan, NhanVien nhanVien) {
    NhaXe nhaXe = nhanVien.getNhaXe();

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
            "NhanVien",
            nhaXe != null ? nhaXe.getMaNhaXe() : null,
            nhaXe != null ? nhaXe.getTenNhaXe() : null
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

  private void updateCustomerProfile(TaiKhoan taiKhoan, UpdateAdminAccountRequest request) {
    KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan)
            .orElseGet(() -> KhachHang.builder()
                    .maKH(generateCode("KH"))
                    .taiKhoan(taiKhoan)
                    .trangThai(taiKhoan.getTrangThaiTK())
                    .build());

    String tenKhachHang = normalizeOptionalText(request.getTenNguoiDung());
    if (khachHang.getTenKH() == null || tenKhachHang != null) {
      khachHang.setTenKH(requireText(tenKhachHang, "Ten khach hang khong duoc de trong."));
    }

    String email = normalizeOptionalText(request.getEmail());
    if (email != null) {
      validateCustomerEmailForUpdate(email, khachHang.getMaKH());
      khachHang.setEmail(email);
    }

    String sdt = normalizeOptionalText(request.getSdt());
    if (sdt != null) {
      validateCustomerPhoneForUpdate(sdt, khachHang.getMaKH());
      khachHang.setSdt(sdt);
    }

    if (request.getNgaySinh() != null) {
      khachHang.setNgaySinh(request.getNgaySinh());
    }
    if (request.getGioiTinh() != null) {
      khachHang.setGioiTinh(normalizeOptionalText(request.getGioiTinh()));
    }
    if (taiKhoan.getTrangThaiTK() != null) {
      khachHang.setTrangThai(taiKhoan.getTrangThaiTK());
    }

    khachHangRepository.save(khachHang);
  }

  private void updateStaffProfile(TaiKhoan taiKhoan, UpdateAdminAccountRequest request) {
    NhanVien nhanVien = nhanVienRepository.findByTaiKhoan(taiKhoan).orElseGet(() -> {
      NhanVien newNhanVien = new NhanVien();
      newNhanVien.setMaNV(generateCode("NV"));
      newNhanVien.setTaiKhoan(taiKhoan);
      newNhanVien.setNgayVaoLam(LocalDate.now());
      newNhanVien.setTrangThai(taiKhoan.getTrangThaiTK());
      return newNhanVien;
    });

    String tenNhanVien = normalizeOptionalText(request.getTenNguoiDung());
    if (nhanVien.getTenNV() == null || tenNhanVien != null) {
      nhanVien.setTenNV(requireText(tenNhanVien, "Ten nhan vien khong duoc de trong."));
    }

    if (nhanVien.getNhaXe() == null || request.getMaNhaXe() != null) {
      String maNhaXe = requireText(request.getMaNhaXe(), "Ma nha xe khong duoc de trong.");
      NhaXe nhaXe = nhaXeRepository.findById(maNhaXe)
              .orElseThrow(() -> new RuntimeException("Khong tim thay nha xe."));
      nhanVien.setNhaXe(nhaXe);
    }

    String email = normalizeOptionalText(request.getEmail());
    if (email != null) {
      validateStaffEmailForUpdate(email, nhanVien.getMaNV());
      nhanVien.setEmail(email);
    }

    String sdt = normalizeOptionalText(request.getSdt());
    if (sdt != null) {
      validateStaffPhoneForUpdate(sdt, nhanVien.getMaNV());
      nhanVien.setSdt(sdt);
    }

    if (request.getNgayVaoLam() != null) {
      nhanVien.setNgayVaoLam(request.getNgayVaoLam());
    }
    if (request.getGioiTinh() != null) {
      nhanVien.setGioiTinh(normalizeOptionalText(request.getGioiTinh()));
    }
    if (taiKhoan.getTrangThaiTK() != null) {
      nhanVien.setTrangThai(taiKhoan.getTrangThaiTK());
    }

    nhanVienRepository.save(nhanVien);
  }

  private void syncProfileStatus(TaiKhoan taiKhoan, String trangThai) {
    khachHangRepository.findByTaiKhoan(taiKhoan).ifPresent(khachHang -> {
      khachHang.setTrangThai(trangThai);
      khachHangRepository.save(khachHang);
    });

    nhanVienRepository.findByTaiKhoan(taiKhoan).ifPresent(nhanVien -> {
      nhanVien.setTrangThai(trangThai);
      nhanVienRepository.save(nhanVien);
    });
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

  private void validateCustomerEmailForUpdate(String email, String currentMaKH) {
    khachHangRepository.findByEmail(email)
            .filter(existing -> !existing.getMaKH().equals(currentMaKH))
            .ifPresent(existing -> {
              throw new RuntimeException("Email da duoc su dung.");
            });
  }

  private void validateCustomerPhoneForUpdate(String sdt, String currentMaKH) {
    khachHangRepository.findBySdt(sdt)
            .filter(existing -> !existing.getMaKH().equals(currentMaKH))
            .ifPresent(existing -> {
              throw new RuntimeException("So dien thoai da duoc su dung.");
            });
  }

  private void validateStaffEmailForUpdate(String email, String currentMaNV) {
    nhanVienRepository.findByEmail(email)
            .filter(existing -> !existing.getMaNV().equals(currentMaNV))
            .ifPresent(existing -> {
              throw new RuntimeException("Email nhan vien da duoc su dung.");
            });
  }

  private void validateStaffPhoneForUpdate(String sdt, String currentMaNV) {
    nhanVienRepository.findBySdt(sdt)
            .filter(existing -> !existing.getMaNV().equals(currentMaNV))
            .ifPresent(existing -> {
              throw new RuntimeException("So dien thoai nhan vien da duoc su dung.");
            });
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
