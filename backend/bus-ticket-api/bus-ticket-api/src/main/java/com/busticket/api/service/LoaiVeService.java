package com.busticket.api.service;

import com.busticket.api.dto.loaive.LoaiVeResponse;
import com.busticket.api.entity.LoaiVe;
import com.busticket.api.repository.LoaiVeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoaiVeService {

  private static final String ACTIVE_STATUS = "Ho\u1EA1t \u0111\u1ED9ng";

  private final LoaiVeRepository loaiVeRepository;

  public List<LoaiVeResponse> getAllLoaiVe() {
    return loaiVeRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
  }

  public List<LoaiVeResponse> getActiveLoaiVe() {
    return loaiVeRepository.findByTrangThaiOrderByMaLoaiVeAsc(ACTIVE_STATUS)
            .stream()
            .map(this::toResponse)
            .toList();
  }

  private LoaiVeResponse toResponse(LoaiVe loaiVe) {
    return new LoaiVeResponse(
            loaiVe.getMaLoaiVe(),
            loaiVe.getTenLoaiVe(),
            loaiVe.getHeSoGia(),
            loaiVe.getMoTa(),
            loaiVe.getTrangThai()
    );
  }
}
