package com.busticket.api.service;

import com.busticket.api.dto.admin.AdminTripResponse;
import com.busticket.api.dto.admin.UpdateAdminTripStatusRequest;
import com.busticket.api.entity.BenXe;
import com.busticket.api.entity.ChuyenXe;
import com.busticket.api.entity.LoaiXe;
import com.busticket.api.entity.NhaXe;
import com.busticket.api.entity.TuyenXe;
import com.busticket.api.entity.Xe;
import com.busticket.api.repository.ChuyenXeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTripService {

  private final ChuyenXeRepository chuyenXeRepository;

  @Transactional(readOnly = true)
  public List<AdminTripResponse> getTrips() {
    return chuyenXeRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(ChuyenXe::getThoiGianKhoiHanh).reversed())
            .map(this::mapToResponse)
            .toList();
  }

  @Transactional
  public AdminTripResponse updateTripStatus(String maChuyen, UpdateAdminTripStatusRequest request) {
    if (request == null || request.getTrangThai() == null || request.getTrangThai().isBlank()) {
      throw new RuntimeException("Vui long chon trang thai chuyen xe.");
    }

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Khong tim thay chuyen xe."));

    chuyenXe.setTrangThai(mapTrangThaiToDatabase(request.getTrangThai()));
    ChuyenXe saved = chuyenXeRepository.save(chuyenXe);

    return mapToResponse(saved);
  }

  private AdminTripResponse mapToResponse(ChuyenXe chuyenXe) {
    Xe xe = chuyenXe.getXe();
    LoaiXe loaiXe = xe != null ? xe.getLoaiXe() : null;
    NhaXe nhaXe = xe != null ? xe.getNhaXe() : null;
    TuyenXe tuyenXe = chuyenXe.getTuyenXe();
    BenXe benDi = tuyenXe != null ? tuyenXe.getBenDi() : null;
    BenXe benDen = tuyenXe != null ? tuyenXe.getBenDen() : null;

    return new AdminTripResponse(
            chuyenXe.getMaChuyen(),
            xe != null ? xe.getMaXe() : null,
            xe != null ? xe.getBienSo() : null,
            xe != null ? xe.getSoLuongGhe() : null,
            xe != null ? xe.getTrangThai() : null,
            loaiXe != null ? loaiXe.getMaLoaiXe() : null,
            loaiXe != null ? loaiXe.getTenLoaiXe() : null,
            nhaXe != null ? nhaXe.getMaNhaXe() : null,
            nhaXe != null ? nhaXe.getTenNhaXe() : null,
            tuyenXe != null ? tuyenXe.getMaTuyen() : null,
            benDi != null ? benDi.getMaBen() : null,
            benDi != null ? benDi.getTenBen() : null,
            benDen != null ? benDen.getMaBen() : null,
            benDen != null ? benDen.getTenBen() : null,
            tuyenXe != null ? tuyenXe.getKhoangCach() : null,
            tuyenXe != null ? tuyenXe.getThoiGianDuKien() : null,
            chuyenXe.getThoiGianKhoiHanh(),
            chuyenXe.getThoiGianDen(),
            chuyenXe.getGiaVe(),
            chuyenXe.getTrangThai()
    );
  }

  private String mapTrangThaiToDatabase(String trangThai) {
    return switch (trangThai.trim()) {
      case "Đang mở bán" -> "Sắp chạy";
      case "Đã khởi hành" -> "Đang chạy";
      case "Đã hoàn thành" -> "Hoàn thành";
      case "Đã hủy" -> "Đã hủy";
      case "Sắp chạy", "Đang chạy", "Hoàn thành" -> trangThai.trim();
      default -> throw new RuntimeException("Trang thai chuyen xe khong hop le.");
    };
  }
}
