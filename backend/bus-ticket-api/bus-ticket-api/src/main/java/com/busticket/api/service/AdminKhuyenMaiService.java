package com.busticket.api.service;

import com.busticket.api.dto.admin.AdminKhuyenMaiResponse;
import com.busticket.api.dto.admin.CreateAdminKhuyenMaiRequest;
import com.busticket.api.dto.admin.UpdateAdminKhuyenMaiRequest;
import com.busticket.api.dto.admin.UpdateAdminKhuyenMaiStatusRequest;
import com.busticket.api.entity.KhuyenMai;
import com.busticket.api.repository.KhuyenMaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminKhuyenMaiService {

  private static final String STATUS_ACTIVE = "\u0110ang \u00E1p d\u1EE5ng";
  private static final String STATUS_EXPIRED = "H\u1EBFt h\u1EA1n";
  private static final String STATUS_PAUSED = "T\u1EA1m d\u1EEBng";

  private final KhuyenMaiRepository khuyenMaiRepository;

  @Transactional(readOnly = true)
  public List<AdminKhuyenMaiResponse> getKhuyenMai() {
    return khuyenMaiRepository.findAll()
            .stream()
            .sorted(Comparator
                    .comparing(KhuyenMai::getNgayBatDau, Comparator.nullsLast(LocalDateTime::compareTo))
                    .reversed()
                    .thenComparing(KhuyenMai::getMaKhuyenMai, Comparator.nullsLast(String::compareTo)))
            .map(this::mapToResponse)
            .toList();
  }

  @Transactional(readOnly = true)
  public AdminKhuyenMaiResponse getKhuyenMaiById(String maKhuyenMai) {
    if (maKhuyenMai == null || maKhuyenMai.isBlank()) {
      throw new RuntimeException("Ma khuyen mai khong duoc de trong.");
    }

    KhuyenMai khuyenMai = khuyenMaiRepository.findById(maKhuyenMai.trim())
            .orElseThrow(() -> new RuntimeException("Khong tim thay khuyen mai."));

    return mapToResponse(khuyenMai);
  }

  @Transactional
  public AdminKhuyenMaiResponse createKhuyenMai(CreateAdminKhuyenMaiRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu khuyen mai khong duoc de trong.");
    }

    String tenKhuyenMai = requireText(request.getTenKhuyenMai(), "Ten khuyen mai khong duoc de trong.");
    String maKhuyenMai = generateMaKhuyenMai();

    validateDiscount(request.getPhanTramGiam(), request.getSoTienGiam());
    validateDateRange(request.getNgayBatDau(), request.getNgayKetThuc());

    KhuyenMai khuyenMai = new KhuyenMai();
    khuyenMai.setMaKhuyenMai(maKhuyenMai);
    khuyenMai.setTenKhuyenMai(tenKhuyenMai);
    khuyenMai.setPhanTramGiam(normalizeDiscount(request.getPhanTramGiam()));
    khuyenMai.setSoTienGiam(normalizeDiscount(request.getSoTienGiam()));
    khuyenMai.setNgayBatDau(request.getNgayBatDau());
    khuyenMai.setNgayKetThuc(request.getNgayKetThuc());
    khuyenMai.setTrangThai(normalizeStatus(request.getTrangThai()));

    return mapToResponse(khuyenMaiRepository.save(khuyenMai));
  }

  @Transactional
  public AdminKhuyenMaiResponse updateKhuyenMai(
          String maKhuyenMai,
          UpdateAdminKhuyenMaiRequest request
  ) {
    if (request == null) {
      throw new RuntimeException("Du lieu khuyen mai khong duoc de trong.");
    }

    String ma = requireText(maKhuyenMai, "Ma khuyen mai khong duoc de trong.");
    KhuyenMai khuyenMai = khuyenMaiRepository.findById(ma)
            .orElseThrow(() -> new RuntimeException("Khong tim thay khuyen mai."));

    BigDecimal phanTramGiam = request.getPhanTramGiam() != null
            ? request.getPhanTramGiam()
            : khuyenMai.getPhanTramGiam();
    BigDecimal soTienGiam = request.getSoTienGiam() != null
            ? request.getSoTienGiam()
            : khuyenMai.getSoTienGiam();
    LocalDateTime ngayBatDau = request.getNgayBatDau() != null
            ? request.getNgayBatDau()
            : khuyenMai.getNgayBatDau();
    LocalDateTime ngayKetThuc = request.getNgayKetThuc() != null
            ? request.getNgayKetThuc()
            : khuyenMai.getNgayKetThuc();

    validateDiscount(phanTramGiam, soTienGiam);
    validateDateRange(ngayBatDau, ngayKetThuc);

    if (request.getTenKhuyenMai() != null) {
      khuyenMai.setTenKhuyenMai(requireText(request.getTenKhuyenMai(), "Ten khuyen mai khong duoc de trong."));
    }

    if (request.getPhanTramGiam() != null) {
      khuyenMai.setPhanTramGiam(normalizeDiscount(request.getPhanTramGiam()));
    }

    if (request.getSoTienGiam() != null) {
      khuyenMai.setSoTienGiam(normalizeDiscount(request.getSoTienGiam()));
    }

    if (request.getNgayBatDau() != null) {
      khuyenMai.setNgayBatDau(request.getNgayBatDau());
    }

    if (request.getNgayKetThuc() != null) {
      khuyenMai.setNgayKetThuc(request.getNgayKetThuc());
    }

    if (request.getTrangThai() != null) {
      khuyenMai.setTrangThai(normalizeStatus(request.getTrangThai()));
    }

    return mapToResponse(khuyenMaiRepository.save(khuyenMai));
  }

  @Transactional
  public AdminKhuyenMaiResponse updateKhuyenMaiStatus(
          String maKhuyenMai,
          UpdateAdminKhuyenMaiStatusRequest request
  ) {
    if (request == null) {
      throw new RuntimeException("Du lieu trang thai khong duoc de trong.");
    }

    String ma = requireText(maKhuyenMai, "Ma khuyen mai khong duoc de trong.");
    KhuyenMai khuyenMai = khuyenMaiRepository.findById(ma)
            .orElseThrow(() -> new RuntimeException("Khong tim thay khuyen mai."));

    khuyenMai.setTrangThai(normalizeStatus(request.getTrangThai()));

    return mapToResponse(khuyenMaiRepository.save(khuyenMai));
  }

  private AdminKhuyenMaiResponse mapToResponse(KhuyenMai khuyenMai) {
    return new AdminKhuyenMaiResponse(
            khuyenMai.getMaKhuyenMai(),
            khuyenMai.getTenKhuyenMai(),
            khuyenMai.getPhanTramGiam(),
            khuyenMai.getSoTienGiam(),
            khuyenMai.getNgayBatDau(),
            khuyenMai.getNgayKetThuc(),
            khuyenMai.getTrangThai()
    );
  }

  private String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new RuntimeException(message);
    }

    return value.trim();
  }

  private String generateMaKhuyenMai() {
    long next = khuyenMaiRepository.count() + 1;
    String maKhuyenMai = "KM" + String.format("%03d", next);

    while (khuyenMaiRepository.existsById(maKhuyenMai)) {
      next++;
      maKhuyenMai = "KM" + String.format("%03d", next);
    }

    return maKhuyenMai;
  }

  private BigDecimal normalizeDiscount(BigDecimal value) {
    if (value == null) {
      return BigDecimal.ZERO;
    }

    return value;
  }
  private void validateDiscount(BigDecimal phanTramGiam, BigDecimal soTienGiam) {
    boolean hasPercent = phanTramGiam != null && phanTramGiam.compareTo(BigDecimal.ZERO) > 0;
    boolean hasAmount = soTienGiam != null && soTienGiam.compareTo(BigDecimal.ZERO) > 0;

    if (!hasPercent && !hasAmount) {
      throw new RuntimeException("Khuyen mai phai co phan tram giam hoac so tien giam.");
    }

    if (phanTramGiam != null
            && (phanTramGiam.compareTo(BigDecimal.ZERO) < 0
            || phanTramGiam.compareTo(BigDecimal.valueOf(100)) > 0)) {
      throw new RuntimeException("Phan tram giam phai nam trong khoang 0 den 100.");
    }

    if (soTienGiam != null && soTienGiam.compareTo(BigDecimal.ZERO) < 0) {
      throw new RuntimeException("So tien giam khong duoc nho hon 0.");
    }
  }

  private void validateDateRange(LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
    if (ngayBatDau != null && ngayKetThuc != null && ngayBatDau.isAfter(ngayKetThuc)) {
      throw new RuntimeException("Ngay bat dau khong duoc sau ngay ket thuc.");
    }
  }

  private String normalizeStatus(String value) {
    if (value == null || value.isBlank()) {
      return STATUS_ACTIVE;
    }

    String status = value.trim();
    if (!STATUS_ACTIVE.equals(status)
            && !STATUS_EXPIRED.equals(status)
            && !STATUS_PAUSED.equals(status)) {
      throw new RuntimeException("Trang thai khuyen mai khong hop le.");
    }

    return status;
  }
}
