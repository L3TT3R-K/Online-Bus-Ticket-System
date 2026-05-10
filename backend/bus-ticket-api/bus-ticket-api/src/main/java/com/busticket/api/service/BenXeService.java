package com.busticket.api.service;

import com.busticket.api.dto.benxe.DiemBenResponse;
import com.busticket.api.dto.staffxe.BenXeResponse;
import com.busticket.api.entity.DiemBen;
import com.busticket.api.repository.BenXeRepository;
import com.busticket.api.repository.DiemBenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BenXeService {

  private final BenXeRepository benXeRepository;
  private final DiemBenRepository diemBenRepository;

  public List<BenXeResponse> getAllBenXe() {
    return benXeRepository.findAllByOrderByTenBenAsc()
            .stream()
            .map(item -> new BenXeResponse(
                    item.getMaBen(),
                    item.getTenBen(),
                    item.getDiaChi()
            ))
            .toList();
  }

  public List<DiemBenResponse> getDiemDonTraByBen(String maBen, String loai) {
    if (maBen == null || maBen.isBlank()) {
      throw new RuntimeException("Mã bến không được để trống.");
    }

    benXeRepository.findById(maBen)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bến xe."));

    List<DiemBen> diemBenList;
    List<String> loaiFilter = getLoaiFilter(loai);

    if (loaiFilter.isEmpty()) {
      diemBenList = diemBenRepository.findByBenXe_MaBenAndTrangThaiOrderByLoaiAscThuTuAsc(
              maBen,
              "Hoạt động"
      );
    } else {
      diemBenList = diemBenRepository.findByBenXe_MaBenAndLoaiInAndTrangThaiOrderByLoaiAscThuTuAsc(
              maBen,
              loaiFilter,
              "Hoạt động"
      );
    }

    return diemBenList.stream()
            .map(this::mapDiemBen)
            .toList();
  }

  private List<String> getLoaiFilter(String loai) {
    if (loai == null || loai.isBlank()) {
      return List.of();
    }

    String normalizedLoai = normalizeLoai(loai);

    if ("DON".equals(normalizedLoai)) {
      return List.of("Đón", "Cả hai");
    }

    if ("TRA".equals(normalizedLoai)) {
      return List.of("Trả", "Cả hai");
    }

    if ("CA HAI".equals(normalizedLoai) || "CA_HAI".equals(normalizedLoai)) {
      return List.of("Đón", "Trả", "Cả hai");
    }

    throw new RuntimeException("Loại điểm không hợp lệ. Chỉ hỗ trợ Đón, Trả hoặc Cả hai.");
  }

  private String normalizeLoai(String loai) {
    String normalized = Normalizer.normalize(loai.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replace("Đ", "D")
            .replace("đ", "d");

    return normalized.toUpperCase(Locale.ROOT);
  }

  private DiemBenResponse mapDiemBen(DiemBen diemBen) {
    return new DiemBenResponse(
            diemBen.getMaDiemBen(),
            diemBen.getBenXe().getMaBen(),
            diemBen.getBenXe().getTenBen(),
            diemBen.getTenDiem(),
            diemBen.getDiaChi(),
            diemBen.getLoai(),
            diemBen.getThuTu()
    );
  }
}
