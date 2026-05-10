package com.busticket.api.service;

import com.busticket.api.dto.chuyenxe.*;
import com.busticket.api.entity.*;
import com.busticket.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  private final BenXeRepository benXeRepository;
  private final DiemBenRepository diemBenRepository;

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
                    getTenDiemHienThi(stop),
                    stop.getThuTu(),
                    stop.getThoiGian()
            ))
            .toList();
  }

  private String getTenDiemHienThi(DiemDonTra stop) {
    if (stop.getDiemBen() != null && stop.getDiemBen().getTenDiem() != null && !stop.getDiemBen().getTenDiem().isBlank()) {
      return stop.getDiemBen().getTenDiem().trim();
    }

    if (stop.getTenDiem() != null && !stop.getTenDiem().isBlank()) {
      return stop.getTenDiem().trim();
    }

    if (stop.getBenXe() != null && stop.getBenXe().getTenBen() != null) {
      return stop.getBenXe().getTenBen();
    }

    return "";
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

  public ChuyenXeDiemDonTraListResponse getDiemDonTraByTrip(String maChuyen) {
    if (maChuyen == null || maChuyen.isBlank()) {
      throw new RuntimeException("Mã chuyến không được để trống.");
    }

    chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe."));

    List<DiemDonTra> stops = diemDonTraRepository.findByChuyenXe_MaChuyenOrderByLoaiAscThuTuAsc(maChuyen);

    return new ChuyenXeDiemDonTraListResponse(
            maChuyen,
            mapStops(stops, false),
            mapStops(stops, true)
    );
  }

  @Transactional
  public ChuyenXeDiemDonTraListResponse saveDiemDonTraByTrip(
          String maChuyen,
          ChuyenXeSaveDiemDonTraRequest request
  ) {
    if (request == null) {
      throw new RuntimeException("Dữ liệu điểm đón trả không được để trống.");
    }

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe."));

    List<DiemDonTra> existingStops = diemDonTraRepository.findByChuyenXe_MaChuyenOrderByLoaiAscThuTuAsc(maChuyen);
    if (existingStops != null && !existingStops.isEmpty()) {
      diemDonTraRepository.deleteAll(existingStops);
      diemDonTraRepository.flush();
    }

    saveStops(chuyenXe, request.getDiemDon(), "Đón");
    saveStops(chuyenXe, request.getDiemTra(), "Trả");

    return getDiemDonTraByTrip(maChuyen);
  }

  private void saveStops(
          ChuyenXe chuyenXe,
          List<ChuyenXeDiemDonTraRequest> stops,
          String loai
  ) {
    if (stops == null || stops.isEmpty()) {
      return;
    }

    int index = 1;
    for (ChuyenXeDiemDonTraRequest request : stops) {
      if (request == null) {
        continue;
      }

      DiemBen diemBen = findDiemBen(request.getMaDiemBen());
      BenXe benXe = findBenXe(request, diemBen);

      DiemDonTra diemDonTra = new DiemDonTra();
      diemDonTra.setMaDiem(generateMaDiem());
      diemDonTra.setChuyenXe(chuyenXe);
      diemDonTra.setDiemBen(diemBen);
      diemDonTra.setBenXe(benXe);
      diemDonTra.setTenDiem(resolveTenDiem(request, diemBen, benXe));
      diemDonTra.setLoai(loai);
      diemDonTra.setThuTu(request.getThuTu() != null && request.getThuTu() > 0 ? request.getThuTu() : index);
      diemDonTra.setThoiGian(
              request.getThoiGian() != null
                      ? request.getThoiGian()
                      : chuyenXe.getThoiGianKhoiHanh().plusMinutes(index * 10L)
      );

      diemDonTraRepository.save(diemDonTra);
      index++;
    }
  }

  private DiemBen findDiemBen(String maDiemBen) {
    if (maDiemBen == null || maDiemBen.isBlank()) {
      return null;
    }

    return diemBenRepository.findById(maDiemBen)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy điểm bến: " + maDiemBen));
  }

  private BenXe findBenXe(ChuyenXeDiemDonTraRequest request, DiemBen diemBen) {
    if (request.getMaBen() != null && !request.getMaBen().isBlank()) {
      return benXeRepository.findById(request.getMaBen())
              .orElseThrow(() -> new RuntimeException("Không tìm thấy bến xe: " + request.getMaBen()));
    }

    if (diemBen != null) {
      return diemBen.getBenXe();
    }

    throw new RuntimeException("Mã bến không được để trống.");
  }

  private String resolveTenDiem(ChuyenXeDiemDonTraRequest request, DiemBen diemBen, BenXe benXe) {
    if (diemBen != null) {
      return diemBen.getTenDiem();
    }

    if (request.getTenDiem() != null && !request.getTenDiem().isBlank()) {
      return request.getTenDiem().trim();
    }

    return benXe.getTenBen();
  }

  private String generateMaDiem() {
    long next = diemDonTraRepository.count() + 1;
    String maDiem = "DD" + String.format("%03d", next);

    while (diemDonTraRepository.existsById(maDiem)) {
      next++;
      maDiem = "DD" + String.format("%03d", next);
    }

    return maDiem;
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
