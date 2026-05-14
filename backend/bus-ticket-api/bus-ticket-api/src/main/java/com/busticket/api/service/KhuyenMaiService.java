package com.busticket.api.service;

import com.busticket.api.dto.khuyenmai.KhuyenMaiResponse;
import com.busticket.api.entity.KhuyenMai;
import com.busticket.api.repository.KhuyenMaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhuyenMaiService {

  private static final String STATUS_ACTIVE = "\u0110ang \u00E1p d\u1EE5ng";

  private final KhuyenMaiRepository khuyenMaiRepository;

  @Transactional(readOnly = true)
  public List<KhuyenMaiResponse> getActiveKhuyenMai() {
    LocalDateTime now = LocalDateTime.now();
    return khuyenMaiRepository.findActivePromotions(STATUS_ACTIVE, now)
            .stream()
            .map(this::mapToResponse)
            .toList();
  }

  private KhuyenMaiResponse mapToResponse(KhuyenMai khuyenMai) {
    return new KhuyenMaiResponse(
            khuyenMai.getMaKhuyenMai(),
            khuyenMai.getTenKhuyenMai(),
            khuyenMai.getPhanTramGiam(),
            khuyenMai.getSoTienGiam(),
            khuyenMai.getNgayBatDau(),
            khuyenMai.getNgayKetThuc(),
            khuyenMai.getTrangThai()
    );
  }
}
