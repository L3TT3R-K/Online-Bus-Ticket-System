package com.busticket.api.service;

import com.busticket.api.dto.chuyenxe.*;
import com.busticket.api.entity.*;
import com.busticket.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChuyenXeService {

  private final ChuyenXeRepository chuyenXeRepository;
  private final DiemDonTraRepository diemDonTraRepository;
  private final KhuyenMaiRepository khuyenMaiRepository;
  private final GheRepository gheRepository;
  private final VeRepository veRepository;

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
    List<ChuyenXeKhuyenMaiResponse> khuyenMai = getPromotionsForTripDate(startTime);

    return results.stream()
            .map(item -> {
              List<DiemDonTra> diemDonList = diemDonTraRepository
                      .findByChuyenXe_MaChuyenAndLoaiOrderByThuTuAsc(item.getMaChuyen(), "Đón");

              List<DiemDonTra> diemTraList = diemDonTraRepository
                      .findByChuyenXe_MaChuyenAndLoaiOrderByThuTuAsc(item.getMaChuyen(), "Trả");

              return new ChuyenXeSearchResponse(
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
                    splitText(item.getImageUrls()),
                    splitText(item.getAmenities()),
                    mapStops(diemDonList, false),
                    mapStops(diemTraList, true),
                    khuyenMai,
                    item.getRating(),
                    item.getReviewCount(),
                    item.getTrangThai()
              );
            })
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

  private List<ChuyenXeDiemDonTraResponse> mapStops(List<DiemDonTra> stops, boolean dropoff) {
    if (stops == null || stops.isEmpty()) {
      return Collections.emptyList();
    }

    return stops.stream()
            .filter(stop -> isDropoff(stop.getLoai()) == dropoff)
            .map(stop -> new ChuyenXeDiemDonTraResponse(
                    stop.getMaDiem(),
                    stop.getTenDiem(),
                    stop.getThuTu(),
                    stop.getThoiGian()
            ))
            .toList();
  }

  private boolean isDropoff(String loai) {
    return loai != null && loai.trim().startsWith("Tr");
  }

  private List<ChuyenXeKhuyenMaiResponse> getPromotionsForTripDate(LocalDateTime tripDate) {
    return khuyenMaiRepository
            .findByNgayBatDauLessThanEqualAndNgayKetThucGreaterThanEqualOrderByNgayKetThucAsc(tripDate, tripDate)
            .stream()
            .map(this::mapPromotion)
            .toList();
  }

  private ChuyenXeKhuyenMaiResponse mapPromotion(KhuyenMai khuyenMai) {
    return new ChuyenXeKhuyenMaiResponse(
            khuyenMai.getMaKhuyenMai(),
            khuyenMai.getTenKhuyenMai(),
            khuyenMai.getPhanTramGiam(),
            khuyenMai.getSoTienGiam(),
            khuyenMai.getNgayBatDau(),
            khuyenMai.getNgayKetThuc()
    );
  }

  public List<ChuyenXeSeatResponse> getSeatMapByTrip(String maChuyen) {
    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe."));

    String maXe = chuyenXe.getXe().getMaXe();

    List<Ghe> gheList = gheRepository.findByXe_MaXeOrderBySoGheAsc(maXe);

    List<String> activeStatuses = List.of(
            "Giữ chỗ",
            "Đã đặt",
            "Đã thanh toán",
            "Đã dùng"
    );

    List<Ve> veList = veRepository.findByChuyenXe_MaChuyenAndTrangThaiIn(
            maChuyen,
            activeStatuses
    );

    Set<String> holdingSet = new HashSet<>();
    Set<String> bookedSet = new HashSet<>();

    for (Ve ve : veList) {
      String soGhe = getSoGhe(ve);
      if (soGhe == null || ve.getTrangThai() == null) {
        continue;
      }

      String trangThai = ve.getTrangThai().trim();

      if ("Giữ chỗ".equalsIgnoreCase(trangThai)) {
        holdingSet.add(soGhe);
      } else if (
              "Đã đặt".equalsIgnoreCase(trangThai) ||
                      "Đã thanh toán".equalsIgnoreCase(trangThai) ||
                      "Đã dùng".equalsIgnoreCase(trangThai)
      ) {
        bookedSet.add(soGhe);
      }
    }

    return gheList.stream()
            .map(ghe -> {
              String soGhe = ghe.getSoGhe() == null ? "" : ghe.getSoGhe().trim();

              String trangThai = "TRONG";

              if (bookedSet.contains(soGhe)) {
                trangThai = "DA_DAT";
              } else if (holdingSet.contains(soGhe)) {
                trangThai = "DANG_GIU";
              }

              return new ChuyenXeSeatResponse(
                      ghe.getMaGhe(),
                      ghe.getSoGhe(),
                      trangThai
              );
            })
            .toList();
  }

  private String getSoGhe(Ve ve) {
    if (ve == null || ve.getGhe() == null || ve.getGhe().getSoGhe() == null) {
      return null;
    }

    return ve.getGhe().getSoGhe().trim();
  }

}
