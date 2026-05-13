package com.busticket.api.service;

import com.busticket.api.dto.admin.AdminCompanyResponse;
import com.busticket.api.dto.admin.CreateAdminCompanyRequest;
import com.busticket.api.dto.admin.UpdateAdminCompanyRequest;
import com.busticket.api.dto.admin.UpdateAdminCompanyStatusRequest;
import com.busticket.api.entity.NhaXe;
import com.busticket.api.repository.NhaXeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCompanyService {

  private final NhaXeRepository nhaXeRepository;

  public List<AdminCompanyResponse> getCompanies() {
    return nhaXeRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(NhaXe::getMaNhaXe).reversed())
            .map(this::mapToResponse)
            .toList();
  }

  @Transactional
  public AdminCompanyResponse createCompany(CreateAdminCompanyRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu nha xe khong duoc de trong.");
    }

    String maNhaXe = requireText(request.getMaNhaXe(), "Ma nha xe khong duoc de trong.");
    String tenNhaXe = requireText(request.getTenNhaXe(), "Ten nha xe khong duoc de trong.");

    if (nhaXeRepository.existsById(maNhaXe)) {
      throw new RuntimeException("Ma nha xe da ton tai.");
    }

    NhaXe nhaXe = new NhaXe();
    nhaXe.setMaNhaXe(maNhaXe);
    nhaXe.setTenNhaXe(tenNhaXe);
    nhaXe.setSdt(normalizeOptionalText(request.getSdt()));
    nhaXe.setEmail(normalizeOptionalText(request.getEmail()));
    nhaXe.setDiaChi(normalizeOptionalText(request.getDiaChi()));
    nhaXe.setMoTa(normalizeOptionalText(request.getMoTa()));
    nhaXe.setTrangThai(normalizeCompanyStatus(request.getTrangThai()));

    return mapToResponse(nhaXeRepository.save(nhaXe));
  }

  @Transactional
  public AdminCompanyResponse updateCompany(String maNhaXe, UpdateAdminCompanyRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu nha xe khong duoc de trong.");
    }

    String ma = requireText(maNhaXe, "Ma nha xe khong duoc de trong.");
    NhaXe nhaXe = nhaXeRepository.findById(ma)
            .orElseThrow(() -> new RuntimeException("Khong tim thay nha xe."));

    if (request.getTenNhaXe() != null) {
      nhaXe.setTenNhaXe(requireText(request.getTenNhaXe(), "Ten nha xe khong duoc de trong."));
    }

    if (request.getSdt() != null) {
      nhaXe.setSdt(normalizeOptionalText(request.getSdt()));
    }

    if (request.getEmail() != null) {
      nhaXe.setEmail(normalizeOptionalText(request.getEmail()));
    }

    if (request.getDiaChi() != null) {
      nhaXe.setDiaChi(normalizeOptionalText(request.getDiaChi()));
    }

    if (request.getMoTa() != null) {
      nhaXe.setMoTa(normalizeOptionalText(request.getMoTa()));
    }

    if (request.getTrangThai() != null) {
      nhaXe.setTrangThai(normalizeCompanyStatus(request.getTrangThai()));
    }

    return mapToResponse(nhaXeRepository.save(nhaXe));
  }

  @Transactional
  public AdminCompanyResponse updateCompanyStatus(
          String maNhaXe,
          UpdateAdminCompanyStatusRequest request
  ) {
    if (request == null) {
      throw new RuntimeException("Du lieu trang thai khong duoc de trong.");
    }

    String ma = requireText(maNhaXe, "Ma nha xe khong duoc de trong.");
    NhaXe nhaXe = nhaXeRepository.findById(ma)
            .orElseThrow(() -> new RuntimeException("Khong tim thay nha xe."));

    nhaXe.setTrangThai(normalizeCompanyStatus(request.getTrangThai()));

    return mapToResponse(nhaXeRepository.save(nhaXe));
  }

  private AdminCompanyResponse mapToResponse(NhaXe nhaXe) {
    return new AdminCompanyResponse(
            nhaXe.getMaNhaXe(),
            nhaXe.getTenNhaXe(),
            nhaXe.getSdt(),
            nhaXe.getEmail(),
            nhaXe.getDiaChi(),
            nhaXe.getMoTa(),
            nhaXe.getTrangThai()
    );
  }

  private String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new RuntimeException(message);
    }

    return value.trim();
  }

  private String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private String normalizeCompanyStatus(String value) {
    if (value == null || value.isBlank()) {
      return "Hoạt động";
    }

    String status = value.trim();
    if (!"Hoạt động".equals(status)
            && !"Tạm ngừng".equals(status)
            && !"Ngừng hoạt động".equals(status)) {
      throw new RuntimeException("Trang thai nha xe khong hop le.");
    }

    return status;
  }
}
