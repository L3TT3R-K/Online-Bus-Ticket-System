package com.busticket.api.service;

import com.busticket.api.dto.auth.LoginRequest;
import com.busticket.api.dto.auth.LoginResponse;
import com.busticket.api.dto.auth.RegisterRequest;
import com.busticket.api.dto.auth.ResetPasswordRequest;
import com.busticket.api.entity.KhachHang;
import com.busticket.api.entity.TaiKhoan;
import com.busticket.api.repository.KhachHangRepository;
import com.busticket.api.repository.TaiKhoanRepository;
import com.busticket.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ACTIVE_STATUS = "Ho\u1ea1t \u0111\u1ed9ng";
    private static final String ASCII_ACTIVE_STATUS = "Hoat dong";
    private static final String PENDING_EMAIL_STATUS = "Ch\u01b0a x\u00e1c minh";

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthTokenService authTokenService;
    private final AuthEmailService authEmailService;

    @Transactional
    public String register(RegisterRequest request) {
        if (request.getTenDangNhap() == null || request.getTenDangNhap().trim().isEmpty()) {
            throw new RuntimeException("Ten dang nhap khong duoc de trong");
        }

        if (request.getMatKhau() == null || request.getMatKhau().trim().isEmpty()) {
            throw new RuntimeException("Mat khau khong duoc de trong");
        }

        if (!request.getMatKhau().equals(request.getXacNhanMatKhau())) {
            throw new RuntimeException("Mat khau xac nhan khong khop");
        }

        if (request.getTenKH() == null || request.getTenKH().trim().isEmpty()) {
            throw new RuntimeException("Ten khach hang khong duoc de trong");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email khong duoc de trong");
        }

        if (taiKhoanRepository.existsByTenDangNhap(request.getTenDangNhap())) {
            throw new RuntimeException("Ten dang nhap da ton tai");
        }

        String email = request.getEmail().trim();

        if (khachHangRepository.existsByEmail(email)) {
            throw new RuntimeException("Email da duoc su dung");
        }

        if (request.getSdt() != null && !request.getSdt().trim().isEmpty()
                && khachHangRepository.existsBySdt(request.getSdt())) {
            throw new RuntimeException("So dien thoai da duoc su dung");
        }

        TaiKhoan taiKhoan = TaiKhoan.builder()
                .tenDangNhap(request.getTenDangNhap().trim())
                .matKhau(passwordEncoder.encode(request.getMatKhau()))
                .quyen("KhachHang")
                .trangThaiTK(PENDING_EMAIL_STATUS)
                .build();

        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

        KhachHang khachHang = KhachHang.builder()
                .maKH(generateMaKH())
                .tenKH(request.getTenKH().trim())
                .sdt(request.getSdt())
                .email(email)
                .taiKhoan(savedTaiKhoan)
                .trangThai(ACTIVE_STATUS)
                .build();

        khachHangRepository.save(khachHang);

        String token = authTokenService.createToken(
                savedTaiKhoan.getMaTK(),
                email,
                AuthTokenService.TYPE_VERIFY_EMAIL,
                60
        );
        authEmailService.sendVerificationEmail(email, token);

        return "Dang ky thanh cong. Vui long kiem tra email de xac thuc tai khoan.";
    }

    private String generateMaKH() {
        return "KH" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String loginIdentifier = request.getTenDangNhap() == null ? "" : request.getTenDangNhap().trim();

        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(loginIdentifier)
                .or(() -> khachHangRepository.findByEmail(loginIdentifier)
                        .map(KhachHang::getTaiKhoan))
                .orElseThrow(() -> new RuntimeException("Ten dang nhap hoac email khong ton tai"));

        String storedPassword = taiKhoan.getMatKhau();
        boolean passwordMatches = passwordEncoder.matches(request.getMatKhau(), storedPassword)
                || request.getMatKhau().equals(storedPassword);

        if (!passwordMatches) {
            throw new RuntimeException("Mat khau khong dung");
        }

        if (PENDING_EMAIL_STATUS.equals(taiKhoan.getTrangThaiTK())) {
            throw new RuntimeException("Vui long xac thuc email truoc khi dang nhap");
        }

        if (!isActive(taiKhoan.getTrangThaiTK())) {
            throw new RuntimeException("Tai khoan da bi khoa");
        }

        String tenKH = khachHangRepository.findByTaiKhoan(taiKhoan)
                .map(KhachHang::getTenKH)
                .orElse("");

        String accessToken = jwtService.generateToken(taiKhoan);

        return new LoginResponse(
                true,
                "Dang nhap thanh cong",
                taiKhoan.getMaTK(),
                taiKhoan.getTenDangNhap(),
                taiKhoan.getQuyen(),
                tenKH,
                accessToken
        );
    }

    @Transactional
    public String verifyEmail(String token) {
        AuthTokenService.AuthTokenRecord record = authTokenService
                .findValidToken(token, AuthTokenService.TYPE_VERIFY_EMAIL)
                .orElseThrow(() -> new RuntimeException("Link xac thuc khong hop le hoac da het han"));

        TaiKhoan taiKhoan = taiKhoanRepository.findById(record.maTK())
                .orElseThrow(() -> new RuntimeException("Tai khoan khong ton tai"));

        taiKhoan.setTrangThaiTK(ACTIVE_STATUS);
        taiKhoanRepository.save(taiKhoan);
        authTokenService.markUsed(record.id());

        return "Xac thuc email thanh cong. Ban co the dang nhap.";
    }

    @Transactional
    public String forgotPassword(String email) {
        String normalizedEmail = email == null ? "" : email.trim();

        if (normalizedEmail.isEmpty()) {
            throw new RuntimeException("Email khong duoc de trong");
        }

        KhachHang khachHang = khachHangRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Email khong ton tai trong he thong"));

        TaiKhoan taiKhoan = khachHang.getTaiKhoan();
        String token = authTokenService.createToken(
                taiKhoan.getMaTK(),
                normalizedEmail,
                AuthTokenService.TYPE_RESET_PASSWORD,
                90
        );
        authEmailService.sendPasswordResetEmail(normalizedEmail, token);

        return "Vui long kiem tra email de dat lai mat khau.";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        if (request.getMatKhauMoi() == null || request.getMatKhauMoi().trim().isEmpty()) {
            throw new RuntimeException("Mat khau moi khong duoc de trong");
        }

        if (!request.getMatKhauMoi().equals(request.getXacNhanMatKhauMoi())) {
            throw new RuntimeException("Mat khau xac nhan khong khop");
        }

        AuthTokenService.AuthTokenRecord record = authTokenService
                .findValidToken(request.getToken(), AuthTokenService.TYPE_RESET_PASSWORD)
                .orElseThrow(() -> new RuntimeException("Link dat lai mat khau khong hop le hoac da het han"));

        TaiKhoan taiKhoan = taiKhoanRepository.findById(record.maTK())
                .orElseThrow(() -> new RuntimeException("Tai khoan khong ton tai"));

        taiKhoan.setMatKhau(passwordEncoder.encode(request.getMatKhauMoi()));
        taiKhoanRepository.save(taiKhoan);
        authTokenService.markUsed(record.id());

        return "Dat lai mat khau thanh cong. Ban co the dang nhap bang mat khau moi.";
    }

    private boolean isActive(String status) {
        return ACTIVE_STATUS.equals(status) || ASCII_ACTIVE_STATUS.equals(status);
    }
}
