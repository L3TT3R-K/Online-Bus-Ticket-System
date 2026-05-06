package com.busticket.api.service;

import com.busticket.api.dto.CreateStaffXeRequest;
import com.busticket.api.dto.StaffXeProjection;
import com.busticket.api.dto.StaffXeResponse;
import com.busticket.api.dto.UpdateXeStatusRequest;
import com.busticket.api.entity.*;
import com.busticket.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffXeService {

  private final TaiKhoanRepository taiKhoanRepository;
  private final NhanVienRepository nhanVienRepository;
  private final StaffXeRepository staffXeRepository;
  private final LoaiXeRepository loaiXeRepository;
  private final HinhAnhRepository hinhAnhRepository;
  private final TienIchRepository tienIchRepository;
  private final TienIchXeRepository tienIchXeRepository;

  public List<StaffXeResponse> getXeCuaNhaXe(Long maTK) {
    NhanVien nhanVien = getNhanVienFromMaTK(maTK);

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();

    List<StaffXeProjection> data = staffXeRepository.findXeByNhaXe(maNhaXe);

    return data.stream()
            .map(item -> new StaffXeResponse(
                    item.getMaXe(),
                    item.getBienSo(),
                    item.getMaLoaiXe(),
                    item.getTenLoaiXe(),
                    item.getSoLuongGhe(),
                    item.getTrangThai(),
                    splitText(item.getImageUrls()),
                    "Ảnh minh họa xe",
                    splitText(item.getAmenities())
            ))
            .toList();
  }

  public StaffXeResponse createXe(Long maTK, CreateStaffXeRequest request) {
    NhanVien nhanVien = getNhanVienFromMaTK(maTK);

    validateCreateXeRequest(request);

    LoaiXe loaiXe = resolveLoaiXe(request);

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();
    String maXe = generateMaXe();

    Xe xe = new Xe();
    xe.setMaXe(maXe);
    xe.setMaNhaXe(maNhaXe);
    xe.setBienSo(request.getBienSo().trim());
    xe.setMaLoaiXe(loaiXe.getMaLoaiXe());
    xe.setSoLuongGhe(request.getSoLuongGhe());
    xe.setTrangThai("Hoạt động");

    staffXeRepository.save(xe);

    saveImages(maXe, request.getImageUrls());
    saveAmenities(maXe, request.getAmenities());

    return new StaffXeResponse(
            xe.getMaXe(),
            xe.getBienSo(),
            loaiXe.getMaLoaiXe(),
            loaiXe.getTenLoaiXe(),
            xe.getSoLuongGhe(),
            xe.getTrangThai(),
            cleanList(request.getImageUrls()),
            request.getImageDesc() == null || request.getImageDesc().trim().isEmpty()
                    ? "Ảnh minh họa xe"
                    : request.getImageDesc().trim(),
            cleanList(request.getAmenities())
    );
  }

  private NhanVien getNhanVienFromMaTK(Long maTK) {
    TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

    if (!"NhanVien".equals(taiKhoan.getQuyen())) {
      throw new RuntimeException("Tài khoản này không phải nhân viên nhà xe");
    }

    if (!"Hoạt động".equals(taiKhoan.getTrangThaiTK())) {
      throw new RuntimeException("Tài khoản đã bị khóa");
    }

    return nhanVienRepository.findByTaiKhoan(taiKhoan)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));
  }

  private void validateCreateXeRequest(CreateStaffXeRequest request) {
    if (request.getBienSo() == null || request.getBienSo().trim().isEmpty()) {
      throw new RuntimeException("Biển số xe không được để trống");
    }

    if (request.getSoLuongGhe() == null || request.getSoLuongGhe() <= 0) {
      throw new RuntimeException("Số lượng ghế phải lớn hơn 0");
    }

    boolean hasMaLoaiXe = request.getMaLoaiXe() != null && !request.getMaLoaiXe().trim().isEmpty();
    boolean hasLoaiXe = request.getLoaiXe() != null && !request.getLoaiXe().trim().isEmpty();

    if (!hasMaLoaiXe && !hasLoaiXe) {
      throw new RuntimeException("Vui lòng chọn loại xe");
    }
  }

  private LoaiXe resolveLoaiXe(CreateStaffXeRequest request) {
    if (request.getMaLoaiXe() != null && !request.getMaLoaiXe().trim().isEmpty()) {
      return loaiXeRepository.findById(request.getMaLoaiXe().trim())
              .orElseThrow(() -> new RuntimeException("Không tìm thấy loại xe"));
    }

    return loaiXeRepository.findByTenLoaiXe(request.getLoaiXe().trim())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy loại xe: " + request.getLoaiXe()));
  }

  private void saveImages(String maXe, List<String> imageUrls) {
    List<String> cleanImages = cleanList(imageUrls);

    for (int i = 0; i < cleanImages.size(); i++) {
      HinhAnh hinhAnh = HinhAnh.builder()
              .maAnh(generateMaAnh())
              .maXe(maXe)
              .url(cleanImages.get(i))
              .thuTu(i + 1)
              .build();

      hinhAnhRepository.save(hinhAnh);
    }
  }

  private void saveAmenities(String maXe, List<String> amenities) {
    List<String> cleanAmenities = cleanList(amenities);

    for (String tenTienIch : cleanAmenities) {
      TienIch tienIch = tienIchRepository.findByTenTienIch(tenTienIch)
              .orElseThrow(() -> new RuntimeException("Không tìm thấy tiện ích: " + tenTienIch));

      TienIchXeId id = new TienIchXeId(maXe, tienIch.getMaTienIch());

      if (!tienIchXeRepository.existsById(id)) {
        TienIchXe tienIchXe = TienIchXe.builder()
                .id(id)
                .build();

        tienIchXeRepository.save(tienIchXe);
      }
    }
  }

  private List<String> cleanList(List<String> values) {
    if (values == null || values.isEmpty()) {
      return Collections.emptyList();
    }

    return values.stream()
            .filter(item -> item != null && !item.trim().isEmpty())
            .map(String::trim)
            .distinct()
            .toList();
  }

  private List<String> splitText(String value) {
    if (value == null || value.trim().isEmpty()) {
      return Collections.emptyList();
    }

    return Arrays.stream(value.split("\\|\\|"))
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .toList();
  }

  private String generateMaXe() {
    return "XE" + UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 8)
            .toUpperCase();
  }

  private String generateMaAnh() {
    return "HA" + UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 8)
            .toUpperCase();
  }

  public StaffXeResponse updateXeStatus(Long maTK, String maXe, UpdateXeStatusRequest request) {
    NhanVien nhanVien = getNhanVienFromMaTK(maTK);

    if (maXe == null || maXe.trim().isEmpty()) {
      throw new RuntimeException("Mã xe không được để trống");
    }

    if (request.getTrangThai() == null || request.getTrangThai().trim().isEmpty()) {
      throw new RuntimeException("Trạng thái không được để trống");
    }

    String trangThai = request.getTrangThai().trim();

    if (!isValidXeStatus(trangThai)) {
      throw new RuntimeException("Trạng thái xe không hợp lệ");
    }

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();

    Xe xe = staffXeRepository.findByMaXeAndMaNhaXe(maXe.trim(), maNhaXe)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xe thuộc nhà xe của bạn"));

    xe.setTrangThai(trangThai);

    Xe savedXe = staffXeRepository.save(xe);

    return buildStaffXeResponse(savedXe);
  }

  private boolean isValidXeStatus(String trangThai) {
    return "Hoạt động".equals(trangThai)
            || "Bảo dưỡng".equals(trangThai)
            || "Ngừng hoạt động".equals(trangThai);
  }

  private StaffXeResponse buildStaffXeResponse(Xe xe) {
    return new StaffXeResponse(
            xe.getMaXe(),
            xe.getBienSo(),
            xe.getMaLoaiXe(),
            null,
            xe.getSoLuongGhe(),
            xe.getTrangThai(),
            Collections.emptyList(),
            "Ảnh minh họa xe",
            Collections.emptyList()
    );
  }
}