package com.busticket.api.service;

import com.busticket.api.dto.admin.AdminBenXeRequest;
import com.busticket.api.dto.admin.AdminBenXeResponse;
import com.busticket.api.dto.admin.AdminDiemBenRequest;
import com.busticket.api.dto.admin.AdminDiemBenResponse;
import com.busticket.api.entity.BenXe;
import com.busticket.api.entity.DiemBen;
import com.busticket.api.repository.BenXeRepository;
import com.busticket.api.repository.DiemBenRepository;
import com.busticket.api.repository.DiemDonTraRepository;
import com.busticket.api.repository.TuyenXeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminBenXeService {

  private static final Set<String> DIEM_BEN_TYPES = Set.of("Đón", "Trả", "Cả hai");
  private static final Set<String> DIEM_BEN_STATUSES = Set.of("Hoạt động", "Tạm ngưng");

  private final BenXeRepository benXeRepository;
  private final DiemBenRepository diemBenRepository;
  private final TuyenXeRepository tuyenXeRepository;
  private final DiemDonTraRepository diemDonTraRepository;

  @Transactional(readOnly = true)
  public List<AdminBenXeResponse> getBenXe(String keyword) {
    String normalizedKeyword = normalizeSearch(keyword);

    return benXeRepository.findAllByOrderByTenBenAsc()
            .stream()
            .filter(item -> matchesKeyword(item, normalizedKeyword))
            .map(this::mapBenXe)
            .toList();
  }

  @Transactional
  public AdminBenXeResponse createBenXe(AdminBenXeRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu ben xe khong duoc de trong.");
    }

    String maBen = requireText(request.getMaBen(), "Ma ben khong duoc de trong.");
    String tenBen = requireText(request.getTenBen(), "Ten ben khong duoc de trong.");

    if (benXeRepository.existsById(maBen)) {
      throw new RuntimeException("Ma ben da ton tai.");
    }

    if (benXeRepository.existsByTenBenIgnoreCase(tenBen)) {
      throw new RuntimeException("Ten ben da ton tai.");
    }

    if (request.getDiemBen() == null || request.getDiemBen().isEmpty()) {
      throw new RuntimeException("Danh sach diem ben khong duoc de trong.");
    }

    BenXe benXe = new BenXe();
    benXe.setMaBen(maBen);
    benXe.setTenBen(tenBen);
    benXe.setDiaChi(normalizeOptionalText(request.getDiaChi()));

    BenXe savedBenXe = benXeRepository.save(benXe);
    saveDiemBen(savedBenXe, request.getDiemBen());

    return mapBenXe(savedBenXe);
  }

  @Transactional
  public AdminBenXeResponse updateBenXe(String maBen, AdminBenXeRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu ben xe khong duoc de trong.");
    }

    BenXe benXe = findBenXe(maBen);

    if (request.getTenBen() != null) {
      String tenBen = requireText(request.getTenBen(), "Ten ben khong duoc de trong.");
      if (!tenBen.equalsIgnoreCase(benXe.getTenBen()) && benXeRepository.existsByTenBenIgnoreCase(tenBen)) {
        throw new RuntimeException("Ten ben da ton tai.");
      }
      benXe.setTenBen(tenBen);
    }

    if (request.getDiaChi() != null) {
      benXe.setDiaChi(normalizeOptionalText(request.getDiaChi()));
    }

    BenXe savedBenXe = benXeRepository.save(benXe);

    if (request.getDiemBen() != null) {
      replaceDiemBen(savedBenXe, request.getDiemBen());
    }

    return mapBenXe(savedBenXe);
  }

  @Transactional
  public void deleteBenXe(String maBen) {
    BenXe benXe = findBenXe(maBen);

    if (tuyenXeRepository.existsByBenDi_MaBenOrBenDen_MaBen(benXe.getMaBen(), benXe.getMaBen())) {
      throw new RuntimeException("Khong the xoa ben xe dang duoc dung trong tuyen xe.");
    }

    if (diemDonTraRepository.existsByBenXe_MaBen(benXe.getMaBen())) {
      throw new RuntimeException("Khong the xoa ben xe dang duoc dung trong chuyen xe.");
    }

    diemBenRepository.deleteByBenXe_MaBen(benXe.getMaBen());
    benXeRepository.delete(benXe);
  }

  private void replaceDiemBen(BenXe benXe, List<AdminDiemBenRequest> requests) {
    List<DiemBen> existingPoints = diemBenRepository.findByBenXe_MaBenOrderByLoaiAscThuTuAsc(benXe.getMaBen());
    Set<String> nextIds = new HashSet<>();

    for (AdminDiemBenRequest request : requests) {
      String maDiemBen = normalizeOptionalText(request.getMaDiemBen());
      if (maDiemBen != null) {
        nextIds.add(maDiemBen);
      }
    }

    for (DiemBen existing : existingPoints) {
      if (!nextIds.contains(existing.getMaDiemBen())) {
        if (diemDonTraRepository.existsByDiemBen_MaDiemBen(existing.getMaDiemBen())) {
          throw new RuntimeException("Khong the xoa diem ben dang duoc dung trong chuyen xe: " + existing.getMaDiemBen());
        }
        diemBenRepository.delete(existing);
      }
    }

    saveDiemBen(benXe, requests);
  }

  private void saveDiemBen(BenXe benXe, List<AdminDiemBenRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      return;
    }

    Set<String> orderKeys = new HashSet<>();
    int index = 1;

    for (AdminDiemBenRequest request : requests) {
      if (request == null) {
        continue;
      }

      String tenDiem = requireText(request.getTenDiem(), "Ten diem ben khong duoc de trong.");
      String loai = normalizeDiemBenType(request.getLoai());
      Integer thuTu = request.getThuTu() != null ? request.getThuTu() : index;
      String trangThai = normalizeDiemBenStatus(request.getTrangThai());

      if (thuTu < 1) {
        throw new RuntimeException("Thu tu diem ben phai lon hon 0.");
      }

      String orderKey = loai + "#" + thuTu;
      if (!orderKeys.add(orderKey)) {
        throw new RuntimeException("Trung loai va thu tu diem ben.");
      }

      String maDiemBen = normalizeOptionalText(request.getMaDiemBen());
      DiemBen diemBen = maDiemBen != null
              ? diemBenRepository.findById(maDiemBen).orElseGet(DiemBen::new)
              : new DiemBen();

      if (diemBen.getBenXe() != null && !benXe.getMaBen().equals(diemBen.getBenXe().getMaBen())) {
        throw new RuntimeException("Diem ben khong thuoc ben xe dang cap nhat.");
      }

      if (diemBen.getMaDiemBen() == null) {
        diemBen.setMaDiemBen(maDiemBen != null ? maDiemBen : generateMaDiemBen(benXe.getMaBen(), index));
      }

      diemBen.setBenXe(benXe);
      diemBen.setTenDiem(tenDiem);
      diemBen.setDiaChi(normalizeOptionalText(request.getDiaChi()));
      diemBen.setLoai(loai);
      diemBen.setThuTu(thuTu);
      diemBen.setTrangThai(trangThai);
      diemBenRepository.save(diemBen);
      index++;
    }
  }

  private BenXe findBenXe(String maBen) {
    String ma = requireText(maBen, "Ma ben khong duoc de trong.");
    return benXeRepository.findById(ma)
            .orElseThrow(() -> new RuntimeException("Khong tim thay ben xe."));
  }

  private AdminBenXeResponse mapBenXe(BenXe benXe) {
    List<AdminDiemBenResponse> diemBen = diemBenRepository.findByBenXe_MaBenOrderByLoaiAscThuTuAsc(benXe.getMaBen())
            .stream()
            .map(this::mapDiemBen)
            .toList();

    return new AdminBenXeResponse(
            benXe.getMaBen(),
            benXe.getTenBen(),
            benXe.getDiaChi(),
            diemBen
    );
  }

  private AdminDiemBenResponse mapDiemBen(DiemBen diemBen) {
    return new AdminDiemBenResponse(
            diemBen.getMaDiemBen(),
            diemBen.getTenDiem(),
            diemBen.getDiaChi(),
            diemBen.getLoai(),
            diemBen.getThuTu(),
            diemBen.getTrangThai()
    );
  }

  private boolean matchesKeyword(BenXe benXe, String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return true;
    }

    return containsKeyword(benXe.getMaBen(), keyword)
            || containsKeyword(benXe.getTenBen(), keyword)
            || containsKeyword(benXe.getDiaChi(), keyword)
            || diemBenRepository.findByBenXe_MaBenOrderByLoaiAscThuTuAsc(benXe.getMaBen())
            .stream()
            .anyMatch(item -> containsKeyword(item.getMaDiemBen(), keyword)
                    || containsKeyword(item.getTenDiem(), keyword)
                    || containsKeyword(item.getDiaChi(), keyword));
  }

  private boolean containsKeyword(String value, String keyword) {
    return value != null && normalizeSearch(value).contains(keyword);
  }

  private String normalizeDiemBenType(String loai) {
    String value = requireText(loai, "Loai diem ben khong duoc de trong.");
    if (!DIEM_BEN_TYPES.contains(value)) {
      throw new RuntimeException("Loai diem ben khong hop le.");
    }
    return value;
  }

  private String normalizeDiemBenStatus(String trangThai) {
    String value = normalizeOptionalText(trangThai);
    if (value == null) {
      return "Hoạt động";
    }
    if (!DIEM_BEN_STATUSES.contains(value)) {
      throw new RuntimeException("Trang thai diem ben khong hop le.");
    }
    return value;
  }

  private String generateMaDiemBen(String maBen, int index) {
    String safeMaBen = maBen.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    String maDiemBen = "DB_" + safeMaBen + "_" + String.format("%02d", index);
    int suffix = index;

    while (diemBenRepository.existsById(maDiemBen)) {
      suffix++;
      maDiemBen = "DB_" + safeMaBen + "_" + String.format("%02d", suffix);
    }

    return maDiemBen;
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

  private String normalizeSearch(String value) {
    if (value == null) {
      return "";
    }

    return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replace("Đ", "D")
            .replace("đ", "d")
            .toLowerCase(Locale.ROOT);
  }
}
