package com.busticket.api.service;

import com.busticket.api.dto.datve.CreateDatVeRequest;
import com.busticket.api.dto.datve.CreateDatVeResponse;
import com.busticket.api.dto.datve.CreateDatVeTicketResponse;
import com.busticket.api.entity.ChuyenXe;
import com.busticket.api.entity.DatVe;
import com.busticket.api.entity.DiemDonTra;
import com.busticket.api.entity.Ghe;
import com.busticket.api.entity.KhachHang;
import com.busticket.api.entity.LoaiVe;
import com.busticket.api.entity.Ve;
import com.busticket.api.repository.ChuyenXeRepository;
import com.busticket.api.repository.DatVeRepository;
import com.busticket.api.repository.DiemDonTraRepository;
import com.busticket.api.repository.GheRepository;
import com.busticket.api.repository.KhachHangRepository;
import com.busticket.api.repository.LoaiVeRepository;
import com.busticket.api.repository.VeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DatVeService {

  private static final String ACTIVE_TICKET_TYPE_STATUS = "Ho\u1EA1t \u0111\u1ED9ng";

  private static final List<String> ACTIVE_SEAT_STATUSES = List.of(
          "Giữ chỗ",
          "Đã đặt",
          "Đã thanh toán",
          "Đã dùng"
  );

  private final DatVeRepository datVeRepository;
  private final ChuyenXeRepository chuyenXeRepository;
  private final GheRepository gheRepository;
  private final KhachHangRepository khachHangRepository;
  private final DiemDonTraRepository diemDonTraRepository;
  private final VeRepository veRepository;
  private final LoaiVeRepository loaiVeRepository;

  @Transactional
  public CreateDatVeResponse create(CreateDatVeRequest request) {
    if (request == null) {
      throw new RuntimeException("Dữ liệu đặt vé không được để trống.");
    }

    String maDatVe = requireText(request.getMaDatVe(), "Mã đặt vé không được để trống.");
    String maChuyen = requireText(request.getMaChuyen(), "Mã chuyến không được để trống.");
    String maKhachHang = requireText(request.getMaKhachHang(), "Mã khách hàng không được để trống.");
    String maDiemDon = requireText(request.getMaDiemDon(), "Mã điểm đón không được để trống.");
    String maDiemTra = requireText(request.getMaDiemTra(), "Mã điểm trả không được để trống.");
    List<String> maGhes = resolveSeatIds(request);

    if (datVeRepository.existsById(maDatVe)) {
      throw new RuntimeException("Mã đặt vé đã tồn tại.");
    }

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe."));

    KhachHang khachHang = khachHangRepository.findById(maKhachHang)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng."));

    DiemDonTra diemDon = diemDonTraRepository.findById(maDiemDon)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy điểm đón."));
    DiemDonTra diemTra = diemDonTraRepository.findById(maDiemTra)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy điểm trả."));

    validateStopForTrip(chuyenXe, diemDon, "Đón");
    validateStopForTrip(chuyenXe, diemTra, "Trả");

    LoaiVe loaiVe = resolveActiveLoaiVe(request.getMaLoaiVe());

    BigDecimal heSoGia = loaiVe.getHeSoGia() != null ? loaiVe.getHeSoGia() : BigDecimal.ONE;
    BigDecimal giaTien = chuyenXe.getGiaVe().multiply(heSoGia);

    List<Ghe> gheList = new ArrayList<>();
    for (String maGhe : maGhes) {
      Ghe ghe = gheRepository.findById(maGhe)
              .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế: " + maGhe));

      validateSeatForTrip(chuyenXe, ghe);
      validateSeatAvailability(maChuyen, maGhe);
      gheList.add(ghe);
    }

    DatVe datVe = new DatVe();
    datVe.setMaDatVe(maDatVe);
    datVe.setKhachHang(khachHang);
    datVe.setNgayDat(LocalDateTime.now());
    datVe.setTrangThai("Chờ thanh toán");
    datVeRepository.save(datVe);

    BigDecimal tongTien = BigDecimal.ZERO;
    List<CreateDatVeTicketResponse> veList = new ArrayList<>();

    for (Ghe ghe : gheList) {
      Ve ve = new Ve();
      ve.setMaVe(generateMaVe());
      ve.setDatVe(datVe);
      ve.setChuyenXe(chuyenXe);
      ve.setGhe(ghe);
      ve.setKhachHang(khachHang);
      ve.setLoaiVe(loaiVe);
      ve.setDiemDon(diemDon);
      ve.setDiemTra(diemTra);
      ve.setGiaTien(giaTien);
      ve.setTrangThai("Giữ chỗ");
      ve.setThoiGianDat(LocalDateTime.now());
      veRepository.save(ve);

      tongTien = tongTien.add(giaTien);
      veList.add(new CreateDatVeTicketResponse(
              ve.getMaVe(),
              ghe.getMaGhe(),
              loaiVe.getMaLoaiVe(),
              loaiVe.getTenLoaiVe(),
              giaTien,
              ve.getTrangThai()
      ));
    }

    return new CreateDatVeResponse(
            datVe.getMaDatVe(),
            chuyenXe.getMaChuyen(),
            veList,
            tongTien,
            datVe.getTrangThai()
    );
  }

  private List<String> resolveSeatIds(CreateDatVeRequest request) {
    Set<String> seatIds = new LinkedHashSet<>();

    if (request.getMaGhes() != null) {
      request.getMaGhes().stream()
              .filter(item -> item != null && !item.isBlank())
              .map(String::trim)
              .forEach(seatIds::add);
    }

    if (request.getMaGhe() != null && !request.getMaGhe().isBlank()) {
      seatIds.add(request.getMaGhe().trim());
    }

    if (seatIds.isEmpty()) {
      throw new RuntimeException("Danh sách mã ghế không được để trống.");
    }

    return new ArrayList<>(seatIds);
  }

  private LoaiVe resolveActiveLoaiVe(String maLoaiVe) {
    if (maLoaiVe == null || maLoaiVe.isBlank()) {
      return loaiVeRepository.findFirstByTrangThaiOrderByMaLoaiVeAsc(ACTIVE_TICKET_TYPE_STATUS)
              .orElseThrow(() -> new RuntimeException("Không tìm thấy loại vé đang hoạt động."));
    }

    return loaiVeRepository.findByMaLoaiVeAndTrangThai(maLoaiVe.trim(), ACTIVE_TICKET_TYPE_STATUS)
            .orElseThrow(() -> new RuntimeException("Loại vé không tồn tại hoặc đã ngừng hoạt động."));
  }

  private void validateStopForTrip(ChuyenXe chuyenXe, DiemDonTra stop, String expectedType) {
    if (stop.getChuyenXe() == null
            || !chuyenXe.getMaChuyen().equals(stop.getChuyenXe().getMaChuyen())) {
      throw new RuntimeException("Điểm " + expectedType.toLowerCase() + " không thuộc chuyến đã chọn.");
    }

    if (stop.getLoai() == null || !expectedType.equalsIgnoreCase(stop.getLoai().trim())) {
      throw new RuntimeException("Điểm " + expectedType.toLowerCase() + " không đúng loại.");
    }
  }

  private void validateSeatForTrip(ChuyenXe chuyenXe, Ghe ghe) {
    if (chuyenXe.getXe() == null
            || ghe.getXe() == null
            || !chuyenXe.getXe().getMaXe().equals(ghe.getXe().getMaXe())) {
      throw new RuntimeException("Ghế không thuộc xe của chuyến đã chọn: " + ghe.getMaGhe());
    }
  }

  private void validateSeatAvailability(String maChuyen, String maGhe) {
    boolean seatUnavailable = veRepository.existsByChuyenXe_MaChuyenAndGhe_MaGheAndTrangThaiIn(
            maChuyen,
            maGhe,
            ACTIVE_SEAT_STATUSES
    );

    if (seatUnavailable) {
      throw new RuntimeException("Ghế đã được giữ hoặc đã được đặt: " + maGhe);
    }
  }

  private String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new RuntimeException(message);
    }

    return value.trim();
  }

  private String generateMaVe() {
    String maVe;

    do {
      maVe = "VE" + System.currentTimeMillis();
    } while (veRepository.existsById(maVe));

    return maVe;
  }
}
