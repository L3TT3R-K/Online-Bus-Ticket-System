package com.busticket.api.service;

import com.busticket.api.dto.staffxe.*;
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
  private final DiemBenRepository diemBenRepository;
  private final NhanVienRepository nhanVienRepository;

  private final XeRepository xeRepository;
  private final TuyenXeRepository tuyenXeRepository;
  private final BenXeRepository benXeRepository;

  private final GheRepository gheRepository;
  private final VeRepository veRepository;

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

  @Transactional
  public StaffChuyenXeResponse updateChuyenXe(Integer maTK, String maChuyen, StaffCreateChuyenXeRequest request) {
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

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe."));

    if (!chuyenXe.getXe().getNhaXe().getMaNhaXe().equals(maNhaXe)) {
      throw new RuntimeException("Không có quyền sửa chuyến này.");
    }

    Xe xe = xeRepository.findByMaXeAndNhaXe_MaNhaXe(request.getMaXe(), maNhaXe)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xe thuộc nhà xe của nhân viên."));

    TuyenXe tuyenXe = findOrCreateTuyenXe(
            request.getMaBenDi(),
            request.getMaBenDen(),
            request
    );

    LocalDateTime thoiGianKhoiHanh = LocalDateTime.of(request.getNgayDi(), request.getGioDi());

    chuyenXe.setXe(xe);
    chuyenXe.setTuyenXe(tuyenXe);
    chuyenXe.setThoiGianKhoiHanh(thoiGianKhoiHanh);

    int soPhutDuKien = tuyenXe.getThoiGianDuKien() != null ? tuyenXe.getThoiGianDuKien() : 240;
    chuyenXe.setThoiGianDen(thoiGianKhoiHanh.plusMinutes(soPhutDuKien));

    chuyenXe.setGiaVe(request.getGiaVe());

    ChuyenXe saved = chuyenXeRepository.save(chuyenXe);

    // Xoá điểm dừng cũ và lưu điểm dừng mới
    List<DiemDonTra> existingStops = diemDonTraRepository.findByChuyenXe_MaChuyenOrderByLoaiAscThuTuAsc(saved.getMaChuyen());
    if (existingStops != null && !existingStops.isEmpty()) {
      diemDonTraRepository.deleteAll(existingStops);
    }

    saveStops(saved, request.getStops(), thoiGianKhoiHanh);

    return mapToResponse(saved);
  }

  @Transactional
  public void deleteChuyenXe(Integer maTK, String maChuyen) {
    NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_MaTK(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên theo tài khoản."));

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe."));

    if (!chuyenXe.getXe().getNhaXe().getMaNhaXe().equals(maNhaXe)) {
      throw new RuntimeException("Không có quyền xóa chuyến này.");
    }

    List<DiemDonTra> existingStops = diemDonTraRepository.findByChuyenXe_MaChuyenOrderByLoaiAscThuTuAsc(chuyenXe.getMaChuyen());
    if (existingStops != null && !existingStops.isEmpty()) {
      diemDonTraRepository.deleteAll(existingStops);
    }

    chuyenXeRepository.delete(chuyenXe);
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

      // Nếu stationId là MADIEMBEN thì lấy BenXe từ DiemBen
      DiemBen diemBenEntity = null;
      if (benXe == null) {
        diemBenEntity = diemBenRepository.findById(stopRequest.getStationId()).orElse(null);
        if (diemBenEntity != null) {
          benXe = diemBenEntity.getBenXe();
        }
      }

      // Nếu vẫn chưa tìm thấy, thử lookup một DiemDonTra hiện có (nếu stationId là MADIEM của DiemDonTra)
      if (benXe == null) {
        DiemDonTra existingPoint = diemDonTraRepository.findById(stopRequest.getStationId()).orElse(null);
        if (existingPoint != null) {
          benXe = existingPoint.getBenXe();
          if (existingPoint.getDiemBen() != null) {
            diemBenEntity = existingPoint.getDiemBen();
          }
        }
      }

      DiemDonTra diem = new DiemDonTra();
      diem.setMaDiem(generateMaDiem());
      diem.setChuyenXe(chuyenXe);

      if (benXe != null) {
        diem.setBenXe(benXe);
        // Nếu chúng ta có DiemBen entity, set luôn
        if (diemBenEntity != null) {
          diem.setDiemBen(diemBenEntity);
          diem.setTenDiem(diemBenEntity.getTenDiem());
        } else {
          diem.setTenDiem(benXe.getTenBen());
        }
      } else if (stopRequest.getName() != null && !stopRequest.getName().isBlank()) {
        // Không tìm thấy benXe; chọn bến mặc định theo loại điểm (bến đi cho pickup, bến đến cho dropoff)
        BenXe defaultBen = "dropoff".equalsIgnoreCase(stopRequest.getType())
                ? chuyenXe.getTuyenXe().getBenDen()
                : chuyenXe.getTuyenXe().getBenDi();
        if (defaultBen != null) {
          diem.setBenXe(defaultBen);
        }
        diem.setTenDiem(stopRequest.getName());
      } else {
        // Cuối cùng, dùng tên tạm từ stationId nhưng phải gán một BenXe để tránh NULL violation
        BenXe defaultBen = "dropoff".equalsIgnoreCase(stopRequest.getType())
                ? chuyenXe.getTuyenXe().getBenDen()
                : chuyenXe.getTuyenXe().getBenDi();
        if (defaultBen != null) {
          diem.setBenXe(defaultBen);
        }
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
            .findByChuyenXe_MaChuyenOrderByLoaiAscThuTuAsc(chuyenXe.getMaChuyen())
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

  @Transactional
  public StaffChuyenXeResponse updateChuyenXeStatus(Integer maTK, String maChuyen, String trangThaiMoi) {
    if (trangThaiMoi == null || trangThaiMoi.isBlank()) {
      throw new RuntimeException("Vui lòng chọn trạng thái chuyến xe.");
    }

    String trangThaiDb = mapTrangThaiToDatabase(trangThaiMoi);

    NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_MaTK(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên theo tài khoản."));

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe."));

    String maNhaXeCuaChuyen = chuyenXe.getXe().getNhaXe().getMaNhaXe();

    if (!maNhaXe.equals(maNhaXeCuaChuyen)) {
      throw new RuntimeException("Bạn không có quyền cập nhật chuyến xe này.");
    }

    chuyenXe.setTrangThai(trangThaiDb);

    ChuyenXe saved = chuyenXeRepository.save(chuyenXe);

    return mapToResponse(saved);
  }

  private String mapTrangThaiToDatabase(String trangThai) {
    if (trangThai == null) return "Sắp chạy";

    return switch (trangThai.trim()) {
      case "Đang mở bán" -> "Sắp chạy";
      case "Đã khởi hành" -> "Đang chạy";
      case "Đã hoàn thành" -> "Hoàn thành";
      case "Đã hủy" -> "Đã hủy";

      // Nếu frontend/backend đã gửi đúng trạng thái DB thì giữ nguyên
      case "Sắp chạy", "Đang chạy", "Hoàn thành" -> trangThai.trim();

      default -> throw new RuntimeException("Trạng thái chuyến xe không hợp lệ.");
    };
  }

  @Transactional
  public StaffChuyenXeResponse updateChuyenXe(
          Integer maTK,
          String maChuyen,
          StaffUpdateChuyenXeRequest request
  ) {
    if (maChuyen == null || maChuyen.isBlank()) {
      throw new RuntimeException("Mã chuyến xe không hợp lệ.");
    }

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

    if (request.getKhoangCach() == null || request.getKhoangCach() <= 0) {
      throw new RuntimeException("Khoảng cách phải lớn hơn 0.");
    }

    if (request.getThoiGianDuKien() == null || request.getThoiGianDuKien() <= 0) {
      throw new RuntimeException("Thời gian dự kiến phải lớn hơn 0.");
    }

    NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_MaTK(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên theo tài khoản."));

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe."));

    if (!chuyenXe.getXe().getNhaXe().getMaNhaXe().equals(maNhaXe)) {
      throw new RuntimeException("Bạn không có quyền cập nhật chuyến xe này.");
    }

    Xe xe = xeRepository.findByMaXeAndNhaXe_MaNhaXe(request.getMaXe(), maNhaXe)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xe thuộc nhà xe của nhân viên."));

    TuyenXe tuyenXe = findOrCreateOrUpdateTuyenXeForUpdate(request);

    LocalDateTime thoiGianKhoiHanh = LocalDateTime.of(
            request.getNgayDi(),
            request.getGioDi()
    );

    chuyenXe.setXe(xe);
    chuyenXe.setTuyenXe(tuyenXe);
    chuyenXe.setThoiGianKhoiHanh(thoiGianKhoiHanh);
    chuyenXe.setThoiGianDen(thoiGianKhoiHanh.plusMinutes(request.getThoiGianDuKien()));
    chuyenXe.setGiaVe(request.getGiaVe());

    ChuyenXe saved = chuyenXeRepository.save(chuyenXe);

    diemDonTraRepository.deleteByChuyenXe_MaChuyen(saved.getMaChuyen());
    saveStops(saved, request.getStops(), thoiGianKhoiHanh);

    return mapToResponse(saved);
  }

  private TuyenXe findOrCreateOrUpdateTuyenXeForUpdate(StaffUpdateChuyenXeRequest request) {
    TuyenXe tuyenXe = tuyenXeRepository
            .findByBenDi_MaBenAndBenDen_MaBen(request.getMaBenDi(), request.getMaBenDen())
            .orElseGet(() -> {
              BenXe benDi = benXeRepository.findById(request.getMaBenDi())
                      .orElseThrow(() -> new RuntimeException("Không tìm thấy bến đi."));

              BenXe benDen = benXeRepository.findById(request.getMaBenDen())
                      .orElseThrow(() -> new RuntimeException("Không tìm thấy bến đến."));

              TuyenXe newTuyen = new TuyenXe();
              newTuyen.setMaTuyen(generateMaTuyen());
              newTuyen.setBenDi(benDi);
              newTuyen.setBenDen(benDen);

              return newTuyen;
            });

    tuyenXe.setKhoangCach(request.getKhoangCach());
    tuyenXe.setThoiGianDuKien(request.getThoiGianDuKien());

    return tuyenXeRepository.save(tuyenXe);
  }

  public List<StaffSeatMapResponse> getSeatMapByTrip(Integer maTK, String maChuyen) {
    NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_MaTK(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên theo tài khoản."));

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe."));

    if (!chuyenXe.getXe().getNhaXe().getMaNhaXe().equals(maNhaXe)) {
      throw new RuntimeException("Bạn không có quyền xem sơ đồ ghế chuyến này.");
    }

    String maXe = chuyenXe.getXe().getMaXe();

    List<Ghe> gheList = gheRepository.findByXe_MaXeOrderBySoGheAsc(maXe);

    // Lấy vé thuộc chuyến với trạng thái Giữ chỗ / Đã đặt / Đã thanh toán
    java.util.List<String> bookedStatuses = java.util.List.of("Giữ chỗ", "Đã đặt", "Đã thanh toán");
    java.util.List<Ve> veList = veRepository.findByChuyenXe_MaChuyenAndTrangThaiIn(maChuyen, bookedStatuses);

    java.util.Set<String> holdingSet = new java.util.HashSet<>();
    java.util.Set<String> bookedSet = new java.util.HashSet<>();

    for (Ve v : veList) {
      String seatNo = getSoGhe(v);
      if (seatNo == null) continue;

      if ("Giữ chỗ".equalsIgnoreCase(v.getTrangThai())) {
        holdingSet.add(seatNo);
      } else if ("Đã đặt".equalsIgnoreCase(v.getTrangThai()) || "Đã thanh toán".equalsIgnoreCase(v.getTrangThai())) {
        bookedSet.add(seatNo);
      }
    }

    return gheList.stream()
            .map(ghe -> {
              String status = "TRONG";
              String soGhe = (ghe.getSoGhe() == null ? "" : ghe.getSoGhe().trim());

              if (bookedSet.contains(soGhe)) {
                status = "DA_DAT";
              } else if (holdingSet.contains(soGhe)) {
                status = "DANG_GIU";
              }

              return new StaffSeatMapResponse(
                      ghe.getMaGhe(),
                      ghe.getSoGhe(),
                      status
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
