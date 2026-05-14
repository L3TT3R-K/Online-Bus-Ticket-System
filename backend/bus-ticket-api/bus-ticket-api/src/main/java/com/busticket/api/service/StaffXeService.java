package com.busticket.api.service;

import com.busticket.api.dto.staffxe.*;
import com.busticket.api.entity.*;
import com.busticket.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
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

  private final GheRepository gheRepository;

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

  @Transactional
  public StaffXeResponse createXe(Long maTK, CreateStaffXeRequest request) {
    NhanVien nhanVien = getNhanVienFromMaTK(maTK);

    validateCreateXeRequest(request);

    LoaiXe loaiXe = resolveLoaiXe(request);

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();
    String maXe = generateMaXe();

    Xe xe = new Xe();
    xe.setMaXe(maXe);
    xe.setNhaXe(nhanVien.getNhaXe());
    xe.setBienSo(request.getBienSo().trim());
    xe.setLoaiXe(loaiXe);
    xe.setSoLuongGhe(request.getSoLuongGhe());
    xe.setTrangThai("Hoạt động");

    Xe savedXe = staffXeRepository.save(xe);

    generateSeatsForBus(savedXe);

    saveImages(maXe, request.getImageUrls());
    saveAmenities(maXe, request.getAmenities());

    return new StaffXeResponse(
            savedXe.getMaXe(),
            savedXe.getBienSo(),
            loaiXe.getMaLoaiXe(),
            loaiXe.getTenLoaiXe(),
            savedXe.getSoLuongGhe(),
            savedXe.getTrangThai(),
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

    if (!RoleUtils.isStaffRole(taiKhoan.getQuyen())) {
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

    Xe xe = staffXeRepository.findByMaXeAndNhaXe_MaNhaXe(maXe.trim(), maNhaXe)
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
            xe.getLoaiXe().getMaLoaiXe(),
            xe.getLoaiXe().getTenLoaiXe(),
            xe.getSoLuongGhe(),
            xe.getTrangThai(),
            Collections.emptyList(),
            "Ảnh minh họa xe",
            Collections.emptyList()
    );
  }

  @Transactional
  public StaffXeResponse updateXe(Long maTK, String maXe, UpdateStaffXeRequest request) {
    NhanVien nhanVien = getNhanVienFromMaTK(maTK);

    if (maXe == null || maXe.trim().isEmpty()) {
      throw new RuntimeException("Mã xe không được để trống");
    }

    validateUpdateXeRequest(request);

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();

    Xe xe = staffXeRepository.findByMaXeAndNhaXe_MaNhaXe(maXe.trim(), maNhaXe)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xe thuộc nhà xe của bạn"));

    String bienSoMoi = request.getBienSo().trim();

    staffXeRepository.findByBienSo(bienSoMoi)
            .ifPresent(existing -> {
              if (!existing.getMaXe().equals(xe.getMaXe())) {
                throw new RuntimeException("Biển số xe đã tồn tại");
              }
            });

    LoaiXe loaiXe = loaiXeRepository.findById(request.getMaLoaiXe().trim())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy loại xe"));

    xe.setBienSo(bienSoMoi);
    xe.setLoaiXe(loaiXe);
    xe.setSoLuongGhe(request.getSoLuongGhe());

    Xe savedXe = staffXeRepository.save(xe);

    hinhAnhRepository.deleteByMaXe(savedXe.getMaXe());
    saveImages(savedXe.getMaXe(), request.getImageUrls());

    tienIchXeRepository.deleteByIdMaXe(savedXe.getMaXe());
    saveAmenities(savedXe.getMaXe(), request.getAmenities());

    return new StaffXeResponse(
            savedXe.getMaXe(),
            savedXe.getBienSo(),
            loaiXe.getMaLoaiXe(),
            loaiXe.getTenLoaiXe(),
            savedXe.getSoLuongGhe(),
            savedXe.getTrangThai(),
            cleanList(request.getImageUrls()),
            request.getImageDesc() == null || request.getImageDesc().trim().isEmpty()
                    ? "Ảnh minh họa xe"
                    : request.getImageDesc().trim(),
            cleanList(request.getAmenities())
    );
  }

  private void validateUpdateXeRequest(UpdateStaffXeRequest request) {
    if (request.getBienSo() == null || request.getBienSo().trim().isEmpty()) {
      throw new RuntimeException("Biển số xe không được để trống");
    }

    if (request.getMaLoaiXe() == null || request.getMaLoaiXe().trim().isEmpty()) {
      throw new RuntimeException("Vui lòng chọn loại xe");
    }

    if (request.getSoLuongGhe() == null || request.getSoLuongGhe() <= 0) {
      throw new RuntimeException("Số lượng ghế phải lớn hơn 0");
    }
  }

  private void generateSeatsForBus(Xe xe) {
    if (gheRepository.existsByXe_MaXe(xe.getMaXe())) {
      return;
    }

    int soLuongGhe = xe.getSoLuongGhe();

    for (int i = 0; i < soLuongGhe; i++) {
      String row = String.valueOf((char) ('A' + (i / 4)));
      int number = (i % 4) + 1;
      String soGhe = row + number;

      Ghe ghe = new Ghe();
      ghe.setMaGhe("G_" + xe.getMaXe() + "_" + soGhe);
      ghe.setXe(xe);
      ghe.setSoGhe(soGhe);

      gheRepository.save(ghe);
    }
  }
}
