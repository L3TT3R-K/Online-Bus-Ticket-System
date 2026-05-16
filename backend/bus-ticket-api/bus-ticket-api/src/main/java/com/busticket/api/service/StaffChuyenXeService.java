package com.busticket.api.service;

import com.busticket.api.dto.staffxe.*;
import com.busticket.api.entity.*;
import com.busticket.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class StaffChuyenXeService {

  private static final String ACTIVE_XE_STATUS = "Ho\u1ea1t \u0111\u1ed9ng";

  private final ChuyenXeRepository chuyenXeRepository;
  private final DiemDonTraRepository diemDonTraRepository;
  private final DiemBenRepository diemBenRepository;
  private final NhanVienRepository nhanVienRepository;

  private final XeRepository xeRepository;
  private final TuyenXeRepository tuyenXeRepository;
  private final BenXeRepository benXeRepository;
  private final TaiKhoanRepository taiKhoanRepository;

  private final GheRepository gheRepository;
  private final VeRepository veRepository;

  public List<StaffChuyenXeResponse> getChuyenXeByStaff(Integer maTK) {
    return getChuyenXeByStaff(maTK, null);
  }

  public List<StaffChuyenXeResponse> getChuyenXeByStaff(Integer maTK, String tenNhaXe) {
    StaffAccessContext accessContext = resolveAccessContext(maTK);

    List<ChuyenXe> chuyenXeList = accessContext.admin()
            ? chuyenXeRepository.findAll()
            : chuyenXeRepository.findByXe_NhaXe_MaNhaXeOrderByThoiGianKhoiHanhDesc(accessContext.maNhaXe());

    return chuyenXeList.stream()
            .filter(chuyenXe -> matchesTenNhaXe(chuyenXe, tenNhaXe))
            .sorted(Comparator.comparing(ChuyenXe::getThoiGianKhoiHanh).reversed())
            .map(this::mapToResponse)
            .toList();
  }

  @Transactional
  public StaffChuyenXeResponse createChuyenXe(Integer maTK, StaffCreateChuyenXeRequest request) {
    validateTripBasicRequest(request);
    validateStopsRequired(request.getStops());

    StaffAccessContext accessContext = resolveAccessContext(maTK);
    Xe xe = findXeForAccess(request.getMaXe(), accessContext);

    validateXeActive(xe);

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

    int soPhutDuKien = tuyenXe.getThoiGianDuKien() != null
            ? tuyenXe.getThoiGianDuKien()
            : 240;

    LocalDateTime thoiGianDen = thoiGianKhoiHanh.plusMinutes(soPhutDuKien);
    validateXeScheduleAvailable(xe.getMaXe(), thoiGianKhoiHanh, thoiGianDen, null);

    chuyenXe.setThoiGianDen(thoiGianDen);
    chuyenXe.setGiaVe(request.getGiaVe());
    chuyenXe.setTrangThai("Sắp chạy");

    ChuyenXe saved = chuyenXeRepository.save(chuyenXe);

    saveStops(saved, request.getStops(), thoiGianKhoiHanh);

    return mapToResponse(saved);
  }

  @Transactional
  public StaffChuyenXeResponse updateChuyenXe(
          Integer maTK,
          String maChuyen,
          StaffCreateChuyenXeRequest request
  ) {
    StaffAccessContext accessContext = resolveAccessContext(maTK);

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Khong tim thay chuyen xe."));

    assertCanAccessTrip(accessContext, chuyenXe, "Ban khong co quyen sua chuyen xe nay.");

    validateTripBasicRequest(request);
    validateStopsIfProvided(request.getStops());

    Xe xe = findXeForAccess(request.getMaXe(), accessContext);

    validateXeActive(xe);

    BenXe benDi = benXeRepository.findById(request.getMaBenDi())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bến đi."));

    BenXe benDen = benXeRepository.findById(request.getMaBenDen())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bến đến."));

    if (benDi.getMaBen().equals(benDen.getMaBen())) {
      throw new RuntimeException("Bến đi và bến đến không được trùng nhau.");
    }

    TuyenXe tuyenXe = findOrCreateTuyenXe(
            request.getMaBenDi(),
            request.getMaBenDen(),
            request
    );

    LocalDateTime thoiGianKhoiHanh = LocalDateTime.of(
            request.getNgayDi(),
            request.getGioDi()
    );

    LocalDateTime thoiGianDen = thoiGianKhoiHanh.plusMinutes(request.getThoiGianDuKien());
    validateXeScheduleAvailable(xe.getMaXe(), thoiGianKhoiHanh, thoiGianDen, maChuyen);

    chuyenXe.setXe(xe);
    chuyenXe.setTuyenXe(tuyenXe);
    chuyenXe.setThoiGianKhoiHanh(thoiGianKhoiHanh);
    chuyenXe.setThoiGianDen(thoiGianDen);
    chuyenXe.setGiaVe(request.getGiaVe());

    ChuyenXe saved = chuyenXeRepository.save(chuyenXe);

    /*
      Quan trọng:
      - Nếu request.stops == null: giữ nguyên điểm đón/trả cũ.
      - Nếu request.stops != null: thay thế điểm đón/trả bằng danh sách mới.
      Như vậy khi frontend chỉ sửa giá vé và không gửi stops thì không bị lỗi DD018 nữa.
    */
    if (request.getStops() != null) {
      List<StaffTripStopRequest> normalizedStops = normalizeStopsBeforeReplace(request.getStops());
      diemDonTraRepository.deleteByChuyenXe_MaChuyen(saved.getMaChuyen());
      diemDonTraRepository.flush();
      saveStops(saved, normalizedStops, thoiGianKhoiHanh);
    }

    return mapToResponse(saved);
  }

  @Transactional
  public StaffChuyenXeResponse updateChuyenXeStatus(Integer maTK, String maChuyen, String trangThaiMoi) {
    if (trangThaiMoi == null || trangThaiMoi.isBlank()) {
      throw new RuntimeException("Vui lòng chọn trạng thái chuyến xe.");
    }

    String trangThaiDb = mapTrangThaiToDatabase(trangThaiMoi);

    StaffAccessContext accessContext = resolveAccessContext(maTK);

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Khong tim thay chuyen xe."));

    assertCanAccessTrip(accessContext, chuyenXe, "Ban khong co quyen cap nhat chuyen xe nay.");

    chuyenXe.setTrangThai(trangThaiDb);

    ChuyenXe saved = chuyenXeRepository.save(chuyenXe);

    return mapToResponse(saved);
  }

  @Transactional
  public void deleteChuyenXe(Integer maTK, String maChuyen) {
    StaffAccessContext accessContext = resolveAccessContext(maTK);

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Khong tim thay chuyen xe."));

    assertCanAccessTrip(accessContext, chuyenXe, "Khong co quyen xoa chuyen nay.");

    List<DiemDonTra> existingStops = diemDonTraRepository
            .findByChuyenXe_MaChuyenOrderByLoaiAscThuTuAsc(chuyenXe.getMaChuyen());

    if (existingStops != null && !existingStops.isEmpty()) {
      diemDonTraRepository.deleteAll(existingStops);
    }

    chuyenXeRepository.delete(chuyenXe);
  }

  public List<StaffSeatMapResponse> getSeatMapByTrip(Integer maTK, String maChuyen) {
    StaffAccessContext accessContext = resolveAccessContext(maTK);

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Khong tim thay chuyen xe."));

    assertCanAccessTrip(accessContext, chuyenXe, "Ban khong co quyen xem so do ghe chuyen nay.");

    String maXe = chuyenXe.getXe().getMaXe();

    List<Ghe> gheList = gheRepository.findByXe_MaXeOrderBySoGheAsc(maXe);

    List<String> bookedStatuses = List.of("Giữ chỗ", "Đã đặt", "Đã thanh toán");
    List<Ve> veList = veRepository.findByChuyenXe_MaChuyenAndTrangThaiIn(maChuyen, bookedStatuses);

    java.util.Set<String> holdingSet = new java.util.HashSet<>();
    java.util.Set<String> bookedSet = new java.util.HashSet<>();

    for (Ve v : veList) {
      String seatNo = getSoGhe(v);
      if (seatNo == null) continue;

      if ("Giữ chỗ".equalsIgnoreCase(v.getTrangThai())) {
        holdingSet.add(seatNo);
      } else if ("Đã đặt".equalsIgnoreCase(v.getTrangThai())
              || "Đã thanh toán".equalsIgnoreCase(v.getTrangThai())) {
        bookedSet.add(seatNo);
      }
    }

    return gheList.stream()
            .map(ghe -> {
              String status = "TRONG";
              String soGhe = ghe.getSoGhe() == null ? "" : ghe.getSoGhe().trim();

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

  private StaffAccessContext resolveAccessContext(Integer maTK) {
    TaiKhoan taiKhoan = taiKhoanRepository.findById(Long.valueOf(maTK))
            .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan."));

    if (RoleUtils.isAdminRole(taiKhoan.getQuyen())) {
      return new StaffAccessContext(true, null);
    }

    NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_MaTK(maTK)
            .orElseThrow(() -> new RuntimeException("Khong tim thay nhan vien theo tai khoan."));

    return new StaffAccessContext(false, nhanVien.getNhaXe().getMaNhaXe());
  }

  private Xe findXeForAccess(String maXe, StaffAccessContext accessContext) {
    if (accessContext.admin()) {
      return xeRepository.findById(maXe)
              .orElseThrow(() -> new RuntimeException("Khong tim thay xe."));
    }

    return xeRepository.findByMaXeAndNhaXe_MaNhaXe(maXe, accessContext.maNhaXe())
            .orElseThrow(() -> new RuntimeException("Khong tim thay xe thuoc nha xe cua nhan vien."));
  }

  private void assertCanAccessTrip(StaffAccessContext accessContext, ChuyenXe chuyenXe, String message) {
    if (accessContext.admin()) {
      return;
    }

    if (chuyenXe.getXe() == null || chuyenXe.getXe().getNhaXe() == null) {
      throw new RuntimeException("Chuyen xe khong hop le.");
    }

    if (!accessContext.maNhaXe().equals(chuyenXe.getXe().getNhaXe().getMaNhaXe())) {
      throw new RuntimeException(message);
    }
  }

  private boolean matchesTenNhaXe(ChuyenXe chuyenXe, String tenNhaXe) {
    if (tenNhaXe == null || tenNhaXe.isBlank()) {
      return true;
    }

    if (chuyenXe.getXe() == null || chuyenXe.getXe().getNhaXe() == null) {
      return false;
    }

    String actual = chuyenXe.getXe().getNhaXe().getTenNhaXe();
    return actual != null && actual.toLowerCase(Locale.ROOT).contains(tenNhaXe.trim().toLowerCase(Locale.ROOT));
  }

  private record StaffAccessContext(boolean admin, String maNhaXe) {
  }

  private TuyenXe findOrCreateTuyenXe(String maBenDi, String maBenDen, StaffCreateChuyenXeRequest request) {
    int khoangCach = request.getKhoangCach() != null && request.getKhoangCach() > 0
            ? request.getKhoangCach()
            : 100;

    int thoiGianDuKien = request.getThoiGianDuKien() != null && request.getThoiGianDuKien() > 0
            ? request.getThoiGianDuKien()
            : 240;

    return tuyenXeRepository.findByBenDi_MaBenAndBenDen_MaBen(maBenDi, maBenDen)
            .map(existing -> {
              existing.setKhoangCach(khoangCach);
              existing.setThoiGianDuKien(thoiGianDuKien);
              return tuyenXeRepository.save(existing);
            })
            .orElseGet(() -> {
              BenXe benDi = benXeRepository.findById(maBenDi)
                      .orElseThrow(() -> new RuntimeException("Không tìm thấy bến đi."));

              BenXe benDen = benXeRepository.findById(maBenDen)
                      .orElseThrow(() -> new RuntimeException("Không tìm thấy bến đến."));

              TuyenXe tuyenXe = new TuyenXe();
              tuyenXe.setMaTuyen(generateMaTuyen());
              tuyenXe.setBenDi(benDi);
              tuyenXe.setBenDen(benDen);
              tuyenXe.setKhoangCach(khoangCach);
              tuyenXe.setThoiGianDuKien(thoiGianDuKien);

              return tuyenXeRepository.save(tuyenXe);
            });
  }

  private void saveStops(
          ChuyenXe chuyenXe,
          List<StaffTripStopRequest> stops,
          LocalDateTime thoiGianKhoiHanh
  ) {
    if (stops == null || stops.isEmpty()) {
      return;
    }

    List<StaffTripStopRequest> pickupStops = stops.stream()
            .filter(stop -> isPickup(stop.getType()))
            .sorted(Comparator.comparing(stop -> stop.getOrder() == null ? 999 : stop.getOrder()))
            .toList();

    List<StaffTripStopRequest> dropoffStops = stops.stream()
            .filter(stop -> isDropoff(stop.getType()))
            .sorted(Comparator.comparing(stop -> stop.getOrder() == null ? 999 : stop.getOrder()))
            .toList();

    saveStopGroup(chuyenXe, pickupStops, "Đón", thoiGianKhoiHanh);
    saveStopGroup(chuyenXe, dropoffStops, "Trả", thoiGianKhoiHanh);
  }

  private List<StaffTripStopRequest> normalizeStopsBeforeReplace(List<StaffTripStopRequest> stops) {
    if (stops == null) {
      return null;
    }

    return stops.stream()
            .map(stop -> {
              DiemBen diemBen = resolveDiemBenFromRequest(stop);

              StaffTripStopRequest normalized = new StaffTripStopRequest();
              normalized.setMaDiemBen(diemBen.getMaDiemBen());
              normalized.setName(stop.getName());
              normalized.setType(stop.getType());
              normalized.setOrder(stop.getOrder());

              return normalized;
            })
            .toList();
  }

  private void saveStopGroup(
          ChuyenXe chuyenXe,
          List<StaffTripStopRequest> stops,
          String loai,
          LocalDateTime thoiGianKhoiHanh
  ) {
    int thuTu = 1;

    for (StaffTripStopRequest stopRequest : stops) {
      if (stopRequest.getMaDiemBen() == null || stopRequest.getMaDiemBen().isBlank()) {
        continue;
      }

      DiemBen diemBen = resolveDiemBenFromRequest(stopRequest);

      DiemDonTra diem = new DiemDonTra();

      diem.setMaDiem(generateMaDiem(chuyenXe.getMaChuyen(), loai, thuTu));
      diem.setChuyenXe(chuyenXe);

      diem.setDiemBen(diemBen);
      diem.setBenXe(diemBen.getBenXe());
      diem.setTenDiem(diemBen.getTenDiem());

      diem.setLoai(loai);
      diem.setThuTu(thuTu);

      if (thoiGianKhoiHanh != null) {
        if ("Đón".equals(loai)) {
          diem.setThoiGian(thoiGianKhoiHanh.plusMinutes((thuTu - 1L) * 15L));
        } else {
          diem.setThoiGian(thoiGianKhoiHanh.plusMinutes(thuTu * 20L));
        }
      }

      diemDonTraRepository.save(diem);

      thuTu++;
    }
  }

  /*
    Hàm này giúp chống lỗi:
    - Frontend gửi đúng MaDiemBen: tìm trong DIEMBEN.
    - Frontend lỡ gửi MaDiem của DIEMDONTRA như DD018: tìm DIEMDONTRA rồi lấy DiemBen gốc.
  */
  private DiemBen resolveDiemBenFromRequest(StaffTripStopRequest stopRequest) {
    String code = stopRequest.getMaDiemBen() == null ? "" : stopRequest.getMaDiemBen().trim();

    if (code.isEmpty()) {
      throw new RuntimeException("Điểm đón/trả thiếu mã điểm bến.");
    }

    return diemBenRepository.findById(code)
            .orElseGet(() -> {
              DiemDonTra existingStop = diemDonTraRepository.findById(code)
                      .orElseThrow(() -> new RuntimeException("Không tìm thấy điểm bến: " + code));

              if (existingStop.getDiemBen() == null) {
                throw new RuntimeException("Điểm " + code + " không liên kết với điểm bến gốc.");
              }

              return existingStop.getDiemBen();
            });
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
            chuyenXe.getXe().getNhaXe().getMaNhaXe(),
            chuyenXe.getXe().getNhaXe().getTenNhaXe(),

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

  private void validateTripBasicRequest(StaffCreateChuyenXeRequest request) {
    if (request == null) {
      throw new RuntimeException("Dữ liệu chuyến xe không hợp lệ.");
    }

    if (request.getMaXe() == null || request.getMaXe().trim().isEmpty()) {
      throw new RuntimeException("Vui lòng chọn xe.");
    }

    if (request.getMaBenDi() == null || request.getMaBenDi().trim().isEmpty()) {
      throw new RuntimeException("Vui lòng chọn bến đi.");
    }

    if (request.getMaBenDen() == null || request.getMaBenDen().trim().isEmpty()) {
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

    if (request.getGiaVe() == null || request.getGiaVe().compareTo(BigDecimal.ZERO) < 0) {
      throw new RuntimeException("Giá vé không hợp lệ.");
    }

    if (request.getKhoangCach() == null || request.getKhoangCach() <= 0) {
      throw new RuntimeException("Khoảng cách phải lớn hơn 0.");
    }

    if (request.getThoiGianDuKien() == null || request.getThoiGianDuKien() <= 0) {
      throw new RuntimeException("Thời gian dự kiến phải lớn hơn 0.");
    }
  }

  private void validateXeScheduleAvailable(
          String maXe,
          LocalDateTime thoiGianKhoiHanh,
          LocalDateTime thoiGianDen,
          String excludeMaChuyen
  ) {
    if (thoiGianDen == null || thoiGianKhoiHanh == null || !thoiGianDen.isAfter(thoiGianKhoiHanh)) {
      throw new RuntimeException("Thời gian đến phải sau thời gian khởi hành.");
    }

    boolean hasOverlap = chuyenXeRepository.existsOverlappingTripByXe(
            maXe,
            thoiGianKhoiHanh,
            thoiGianDen,
            excludeMaChuyen
    );

    if (hasOverlap) {
      throw new RuntimeException("Xe " + maXe + " đã có chuyến trong khoảng thời gian này.");
    }
  }

  private void validateXeActive(Xe xe) {
    if (xe == null || !ACTIVE_XE_STATUS.equals(xe.getTrangThai())) {
      throw new RuntimeException("Ch\u1ec9 \u0111\u01b0\u1ee3c ch\u1ecdn xe \u0111ang ho\u1ea1t \u0111\u1ed9ng \u0111\u1ec3 t\u1ea1o chuy\u1ebfn.");
    }
  }

  private void validateStopsRequired(List<StaffTripStopRequest> stops) {
    if (stops == null || stops.isEmpty()) {
      throw new RuntimeException("Vui lòng chọn điểm đón/trả.");
    }

    validateStopsCommon(stops);
  }

  private void validateStopsIfProvided(List<StaffTripStopRequest> stops) {
    if (stops == null) {
      return;
    }

    if (stops.isEmpty()) {
      throw new RuntimeException("Danh sách điểm đón/trả không được rỗng.");
    }

    validateStopsCommon(stops);
  }

  private void validateStopsCommon(List<StaffTripStopRequest> stops) {
    boolean hasPickup = stops.stream()
            .anyMatch(stop -> isPickup(stop.getType()));

    boolean hasDropoff = stops.stream()
            .anyMatch(stop -> isDropoff(stop.getType()));

    if (!hasPickup) {
      throw new RuntimeException("Vui lòng chọn ít nhất một điểm đón.");
    }

    if (!hasDropoff) {
      throw new RuntimeException("Vui lòng chọn ít nhất một điểm trả.");
    }

    for (StaffTripStopRequest stop : stops) {
      if (stop.getMaDiemBen() == null || stop.getMaDiemBen().trim().isEmpty()) {
        throw new RuntimeException("Điểm đón/trả thiếu mã điểm bến.");
      }

      if (stop.getType() == null || stop.getType().trim().isEmpty()) {
        throw new RuntimeException("Điểm đón/trả thiếu loại điểm.");
      }

      if (!isPickup(stop.getType()) && !isDropoff(stop.getType())) {
        throw new RuntimeException("Loại điểm đón/trả không hợp lệ.");
      }

      if (stop.getOrder() != null && stop.getOrder() <= 0) {
        throw new RuntimeException("Thứ tự điểm đón/trả phải lớn hơn 0.");
      }
    }
  }

  private boolean isPickup(String type) {
    if (type == null) return false;

    String value = type.trim().toLowerCase();

    return value.equals("pickup")
            || value.equals("don")
            || value.equals("đón")
            || value.equals("diemdon")
            || value.equals("điểm đón");
  }

  private boolean isDropoff(String type) {
    if (type == null) return false;

    String value = type.trim().toLowerCase();

    return value.equals("dropoff")
            || value.equals("tra")
            || value.equals("trả")
            || value.equals("diemtra")
            || value.equals("điểm trả");
  }

  private String getSoGhe(Ve ve) {
    if (ve == null || ve.getGhe() == null || ve.getGhe().getSoGhe() == null) {
      return null;
    }

    return ve.getGhe().getSoGhe().trim();
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

  private String mapTrangThaiToDatabase(String trangThai) {
    if (trangThai == null) return "Sắp chạy";

    return switch (trangThai.trim()) {
      case "Đang mở bán" -> "Sắp chạy";
      case "Đã khởi hành" -> "Đang chạy";
      case "Đã hoàn thành" -> "Hoàn thành";
      case "Đã hủy" -> "Đã hủy";

      case "Sắp chạy", "Đang chạy", "Hoàn thành" -> trangThai.trim();

      default -> throw new RuntimeException("Trạng thái chuyến xe không hợp lệ.");
    };
  }

  private String generateMaChuyen() {
    long next = chuyenXeRepository.countBy() + 1;
    return "CX" + String.format("%03d", next);
  }

  private String generateMaTuyen() {
    long next = tuyenXeRepository.count() + 1;
    return "T" + String.format("%03d", next);
  }

  private String generateMaDiem(String maChuyen, String loai, int thuTu) {
    String prefix = "Đón".equals(loai) ? "DD" : "DT";
    return prefix + "_" + maChuyen + "_" + String.format("%02d", thuTu);
  }
}
