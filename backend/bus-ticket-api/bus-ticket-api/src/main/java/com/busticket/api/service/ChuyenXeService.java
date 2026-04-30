package com.busticket.api.service;

import com.busticket.api.dto.ChuyenXeSearchProjection;
import com.busticket.api.dto.ChuyenXeSearchResponse;
import com.busticket.api.repository.ChuyenXeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChuyenXeService {

  private final ChuyenXeRepository chuyenXeRepository;

  public List<ChuyenXeSearchResponse> searchChuyenXe(String diemDen, String diemDi, LocalDate ngayDi) {
    if (diemDi == null || diemDi.trim().isEmpty()) {
      throw new RuntimeException("Điểm đi không được để trống");
    }

    if (diemDen == null || diemDen.trim().isEmpty()) {
      throw new RuntimeException("Điểm đến không được để trống");
    }

    if (ngayDi == null) {
      throw new RuntimeException("Ngày đi không được để trống");
    }
    LocalDateTime startTime = ngayDi.atStartOfDay();
    LocalDateTime endTime = ngayDi.plusDays(1).atStartOfDay();

    List<ChuyenXeSearchProjection> results = chuyenXeRepository.searchChuyenXe(
            diemDi.trim(),
            diemDen.trim(),
            startTime,
            endTime
    );

    return results.stream()
            .map(item -> new ChuyenXeSearchResponse(
                    item.getMaChuyen(),
                    item.getTenNhaXe(),
                    item.getBienSo(),
                    item.getTenLoaiXe(),
                    item.getDiemDi(),
                    item.getDiemDen(),
                    item.getThoiGianKhoiHanh(),
                    item.getThoiGianDen(),
                    item.getGiaVe(),
                    item.getSoLuongGhe(),
                    item.getSoGheDaDat(),
                    item.getSoGheTrong(),
                    item.getTrangThai()
            ))
            .toList();

  }
}
