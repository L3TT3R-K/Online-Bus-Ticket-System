package com.busticket.api.service;

import com.busticket.api.dto.staffxe.StaffChuyenXeResponse;
import com.busticket.api.dto.staffxe.StaffCreateChuyenXeRequest;
import com.busticket.api.dto.staffxe.StaffDiemDonTraResponse;
import com.busticket.api.dto.staffxe.StaffTripStopRequest;
import com.busticket.api.entity.*;
import com.busticket.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffChuyenXeService {

  private final ChuyenXeRepository chuyenXeRepository;
  private final DiemDonTraRepository diemDonTraRepository;
  private final NhanVienRepository nhanVienRepository;

  private final XeRepository xeRepository;
  private final TuyenXeRepository tuyenXeRepository;
  private final BenXeRepository benXeRepository;

  public List<StaffChuyenXeResponse> getChuyenXeByStaff(Integer maTK) {
    NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_MaTK(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên theo tài khoản."));

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();

    return chuyenXeRepository.findByXe_NhaXe_MaNhaXeOrderByThoiGianKhoiHanhDesc(maNhaXe)
            .stream()
            .map(this::mapToResponse)
            .toList();
  }

  @Transactional
  public StaffChuyenXeResponse createChuyenXe(Integer maTK, StaffCreateChuyenXeRequest request) {
    if (request.getMaXe() == null || request.getMaXe().isBlank()) {
      throw new RuntimeException("Vui lòng chọn xe.");
    }

    if (request.getMaBenDi() == null || request.getMaBenDi().isBlank()) {
      throw new RuntimeException("Vui lòng chọn bến đi.");
    }

    if (request.getMaBenDen() == null || request.getMaBenDen().isBlank()) {
      throw new RuntimeException("Vui lòng chọn bến đến.");
    }

    if (request.getMaBenDi().equals(request.getMaBenDen())) {
      throw new RuntimeException("Bến đi và bến đến không được trùng nhau.");
    }

    if (request.getNgayDi() == null) {
      throw new RuntimeException("Vui lòng chọn ngày đi.");
    }

    if (request.getGioDi() == null) {
      throw new RuntimeException("Vui lòng chọn giờ đi.");
    }

    if (request.getGiaVe() == null || request.getGiaVe().signum() < 0) {
      throw new RuntimeException("Giá vé không hợp lệ.");
    }

    NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_MaTK(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên theo tài khoản."));

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();

    Xe xe = xeRepository.findByMaXeAndNhaXe_MaNhaXe(request.getMaXe(), maNhaXe)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xe thuộc nhà xe của nhân viên."));

    TuyenXe tuyenXe = findOrCreateTuyenXe(
            request.getMaBenDi(),
            request.getMaBenDen(),
            request
    );

    LocalDateTime thoiGianKhoiHanh = LocalDateTime.of(request.getNgayDi(), request.getGioDi());

    ChuyenXe chuyenXe = new ChuyenXe();
    chuyenXe.setMaChuyen(generateMaChuyen());
    chuyenXe.setXe(xe);
    chuyenXe.setTuyenXe(tuyenXe);
    chuyenXe.setThoiGianKhoiHanh(thoiGianKhoiHanh);

    // Tạm tính thời gian đến = thời gian khởi hành + thời gian dự kiến của tuyến.
    // Nếu tuyến chưa có thời gian dự kiến thì cộng 4 tiếng.
    int soPhutDuKien = tuyenXe.getThoiGianDuKien() != null ? tuyenXe.getThoiGianDuKien() : 240;
    chuyenXe.setThoiGianDen(thoiGianKhoiHanh.plusMinutes(soPhutDuKien));

    chuyenXe.setGiaVe(request.getGiaVe());
    chuyenXe.setTrangThai("Sắp chạy");

    ChuyenXe saved = chuyenXeRepository.save(chuyenXe);

    saveStops(saved, request.getStops(), thoiGianKhoiHanh);

    return mapToResponse(saved);
  }

  private TuyenXe findOrCreateTuyenXe(String maBenDi, String maBenDen, StaffCreateChuyenXeRequest request) {
    return tuyenXeRepository.findByBenDi_MaBenAndBenDen_MaBen(maBenDi, maBenDen)
            .orElseGet(() -> {
              BenXe benDi = benXeRepository.findById(maBenDi)
                      .orElseThrow(() -> new RuntimeException("Không tìm thấy bến đi."));

              BenXe benDen = benXeRepository.findById(maBenDen)
                      .orElseThrow(() -> new RuntimeException("Không tìm thấy bến đến."));

              int khoangCach = request.getKhoangCach() != null && request.getKhoangCach() > 0
                      ? request.getKhoangCach()
                      : 100;

              int thoiGianDuKien = request.getThoiGianDuKien() != null && request.getThoiGianDuKien() > 0
                      ? request.getThoiGianDuKien()
                      : 240;

              TuyenXe tuyenXe = new TuyenXe();
              tuyenXe.setMaTuyen(generateMaTuyen());
              tuyenXe.setBenDi(benDi);
              tuyenXe.setBenDen(benDen);
              tuyenXe.setKhoangCach(khoangCach);
              tuyenXe.setThoiGianDuKien(thoiGianDuKien);

              return tuyenXeRepository.save(tuyenXe);
            });
  }

  private void saveStops(ChuyenXe chuyenXe, List<StaffTripStopRequest> stops, LocalDateTime thoiGianKhoiHanh) {
    if (stops == null || stops.isEmpty()) return;

    List<StaffTripStopRequest> sortedStops = stops.stream()
            .sorted(Comparator.comparing(stop -> stop.getOrder() == null ? 999 : stop.getOrder()))
            .toList();

    int index = 1;

    for (StaffTripStopRequest stopRequest : sortedStops) {
      if (stopRequest.getStationId() == null || stopRequest.getStationId().isBlank()) {
        continue;
      }

      BenXe benXe = benXeRepository.findById(stopRequest.getStationId())
              .orElse(null);

      DiemDonTra diem = new DiemDonTra();
      diem.setMaDiem(generateMaDiem());
      diem.setChuyenXe(chuyenXe);

      if (benXe != null) {
        diem.setTenDiem(benXe.getTenBen());
      } else if (stopRequest.getName() != null && !stopRequest.getName().isBlank()) {
        diem.setTenDiem(stopRequest.getName());
      } else {
        diem.setTenDiem(stopRequest.getStationId());
      }

      diem.setLoai(mapStopType(stopRequest.getType()));
      diem.setThuTu(index);
      diem.setThoiGian(thoiGianKhoiHanh.plusMinutes(index * 10L));

      diemDonTraRepository.save(diem);
      index++;
    }
  }

  private String mapStopType(String type) {
    if ("dropoff".equalsIgnoreCase(type)) {
      return "Trả";
    }

    return "Đón";
  }

  private String generateMaChuyen() {
    long next = chuyenXeRepository.countBy() + 1;
    return "CX" + String.format("%03d", next);
  }

  private String generateMaTuyen() {
    long next = tuyenXeRepository.count() + 1;
    return "T" + String.format("%03d", next);
  }

  private String generateMaDiem() {
    long next = diemDonTraRepository.count() + 1;
    return "DD" + String.format("%03d", next);
  }

  private StaffChuyenXeResponse mapToResponse(ChuyenXe chuyenXe) {
    LocalDateTime thoiGianKhoiHanh = chuyenXe.getThoiGianKhoiHanh();

    int soLuongGhe = chuyenXe.getXe().getSoLuongGhe();
    int gheTrong = soLuongGhe;

    List<StaffDiemDonTraResponse> stops = diemDonTraRepository
            .findByChuyenXe_MaChuyenOrderByThuTuAsc(chuyenXe.getMaChuyen())
            .stream()
            .map(this::mapStopToResponse)
            .toList();

    String tenBenDi = chuyenXe.getTuyenXe().getBenDi().getTenBen();
    String tenBenDen = chuyenXe.getTuyenXe().getBenDen().getTenBen();
    String tenTuyen = tenBenDi + " - " + tenBenDen;

    return new StaffChuyenXeResponse(
            chuyenXe.getMaChuyen(),

            chuyenXe.getXe().getMaXe(),
            chuyenXe.getXe().getBienSo(),
            chuyenXe.getXe().getLoaiXe().getMaLoaiXe(),
            chuyenXe.getXe().getLoaiXe().getTenLoaiXe(),
            chuyenXe.getXe().getSoLuongGhe(),

            chuyenXe.getTuyenXe().getMaTuyen(),

            chuyenXe.getTuyenXe().getBenDi().getMaBen(),
            tenBenDi,

            chuyenXe.getTuyenXe().getBenDen().getMaBen(),
            tenBenDen,

            tenTuyen,

            thoiGianKhoiHanh.toLocalDate(),
            thoiGianKhoiHanh.toLocalTime(),

            chuyenXe.getGiaVe(),
            gheTrong,

            mapTrangThaiForFrontend(chuyenXe.getTrangThai()),

            chuyenXe.getTuyenXe().getKhoangCach(),
            chuyenXe.getTuyenXe().getThoiGianDuKien(),

            stops
    );
  }

  private StaffDiemDonTraResponse mapStopToResponse(DiemDonTra diemDonTra) {
    return new StaffDiemDonTraResponse(
            diemDonTra.getMaDiem(),
            diemDonTra.getTenDiem(),
            diemDonTra.getLoai(),
            diemDonTra.getThuTu(),
            diemDonTra.getThoiGian()
    );
  }

  private String mapTrangThaiForFrontend(String trangThai) {
    if (trangThai == null) return "Không rõ";

    return switch (trangThai) {
      case "Sắp chạy" -> "Đang mở bán";
      case "Đang chạy" -> "Đã khởi hành";
      case "Hoàn thành" -> "Đã khởi hành";
      case "Đã hủy" -> "Đã hủy";
      default -> trangThai;
    };
  }
}