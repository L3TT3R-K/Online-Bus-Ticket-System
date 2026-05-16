package com.busticket.api.service;

import com.busticket.api.dto.admin.AdminBookingResponse;
import com.busticket.api.dto.admin.UpdateAdminBookingRequest;
import com.busticket.api.entity.BenXe;
import com.busticket.api.entity.ChuyenXe;
import com.busticket.api.entity.DatVe;
import com.busticket.api.entity.Ghe;
import com.busticket.api.entity.HoaDon;
import com.busticket.api.entity.KhachHang;
import com.busticket.api.entity.NhaXe;
import com.busticket.api.entity.ThanhToan;
import com.busticket.api.entity.TuyenXe;
import com.busticket.api.entity.Ve;
import com.busticket.api.entity.Xe;
import com.busticket.api.repository.DatVeRepository;
import com.busticket.api.repository.HoaDonRepository;
import com.busticket.api.repository.ThanhToanRepository;
import com.busticket.api.repository.VeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminBookingService {

  private static final Set<String> DAT_VE_STATUSES = Set.of(
          "Chờ thanh toán",
          "Đã thanh toán",
          "Đã hủy"
  );
  private static final Set<String> HOA_DON_STATUSES = Set.of(
          "Chưa thanh toán",
          "Đã thanh toán",
          "Đã hủy"
  );
  private static final Set<String> THANH_TOAN_STATUSES = Set.of(
          "Đang xử lý",
          "Thành công",
          "Không thành công",
          "Đã hủy"
  );

  private final DatVeRepository datVeRepository;
  private final VeRepository veRepository;
  private final HoaDonRepository hoaDonRepository;
  private final ThanhToanRepository thanhToanRepository;

  @Transactional(readOnly = true)
  public List<AdminBookingResponse> getBookings() {
    return datVeRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(DatVe::getNgayDat).reversed())
            .map(this::mapToResponse)
            .toList();
  }

  @Transactional
  public AdminBookingResponse cancelTicket(String maVe) {
    if (maVe == null || maVe.isBlank()) {
      throw new RuntimeException("Ma ve khong duoc de trong.");
    }

    Ve ve = veRepository.findById(maVe.trim())
            .orElseThrow(() -> new RuntimeException("Khong tim thay ve."));

    DatVe datVe = ve.getDatVe();
    if (datVe == null) {
      throw new RuntimeException("Ve chua lien ket voi don dat ve.");
    }

    ve.setTrangThai("Đã hủy");
    veRepository.save(ve);

    datVe.setTrangThai("Đã hủy");
    DatVe savedDatVe = datVeRepository.save(datVe);

    return mapToResponse(savedDatVe);
  }

  @Transactional
  public AdminBookingResponse updateBooking(String maDatVe, UpdateAdminBookingRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu cap nhat dat ve khong duoc de trong.");
    }

    DatVe datVe = findBooking(maDatVe);

    if (request.getTrangThaiDatVe() != null && !request.getTrangThaiDatVe().isBlank()) {
      String trangThaiDatVe = request.getTrangThaiDatVe().trim();
      validateStatus(DAT_VE_STATUSES, trangThaiDatVe, "Trang thai dat ve khong hop le.");
      datVe.setTrangThai(trangThaiDatVe);
      updateTicketStatuses(datVe.getMaDatVe(), trangThaiDatVe);
    }

    HoaDon hoaDon = hoaDonRepository.findByDatVe_MaDatVe(datVe.getMaDatVe()).orElse(null);

    if (hoaDon != null && request.getTrangThaiHoaDon() != null && !request.getTrangThaiHoaDon().isBlank()) {
      String trangThaiHoaDon = request.getTrangThaiHoaDon().trim();
      validateStatus(HOA_DON_STATUSES, trangThaiHoaDon, "Trang thai hoa don khong hop le.");
      hoaDon.setTrangThai(trangThaiHoaDon);
      hoaDonRepository.save(hoaDon);
    }

    if (hoaDon != null && request.getTrangThaiThanhToan() != null && !request.getTrangThaiThanhToan().isBlank()) {
      ThanhToan thanhToan = thanhToanRepository.findByHoaDon_MaHoaDon(hoaDon.getMaHoaDon()).orElse(null);

      if (thanhToan != null) {
        String trangThaiThanhToan = request.getTrangThaiThanhToan().trim();
        validateStatus(THANH_TOAN_STATUSES, trangThaiThanhToan, "Trang thai thanh toan khong hop le.");
        thanhToan.setTrangThai(trangThaiThanhToan);
        thanhToanRepository.save(thanhToan);
      }
    }

    return mapToResponse(datVeRepository.save(datVe));
  }

  @Transactional
  public void deleteBooking(String maDatVe) {
    DatVe datVe = findBooking(maDatVe);
    HoaDon hoaDon = hoaDonRepository.findByDatVe_MaDatVe(datVe.getMaDatVe()).orElse(null);

    if (hoaDon != null) {
      thanhToanRepository.findByHoaDon_MaHoaDon(hoaDon.getMaHoaDon())
              .ifPresent(thanhToanRepository::delete);
      hoaDonRepository.delete(hoaDon);
    }

    List<Ve> veList = veRepository.findByDatVe_MaDatVe(datVe.getMaDatVe());
    if (!veList.isEmpty()) {
      veRepository.deleteAll(veList);
    }

    datVeRepository.delete(datVe);
  }

  private DatVe findBooking(String maDatVe) {
    if (maDatVe == null || maDatVe.isBlank()) {
      throw new RuntimeException("Ma dat ve khong duoc de trong.");
    }

    return datVeRepository.findById(maDatVe.trim())
            .orElseThrow(() -> new RuntimeException("Khong tim thay don dat ve."));
  }

  private void updateTicketStatuses(String maDatVe, String trangThaiDatVe) {
    List<Ve> veList = veRepository.findByDatVe_MaDatVe(maDatVe);

    if (veList.isEmpty()) {
      return;
    }

    String ticketStatus = switch (trangThaiDatVe) {
      case "Chờ thanh toán" -> "Giữ chỗ";
      case "Đã hủy" -> "Đã hủy";
      case "Đã thanh toán" -> "Đã thanh toán";
      default -> null;
    };

    if (ticketStatus == null) {
      return;
    }

    veList.forEach(ve -> ve.setTrangThai(ticketStatus));
    veRepository.saveAll(veList);
  }

  private void validateStatus(Set<String> allowedStatuses, String status, String message) {
    if (!allowedStatuses.contains(status)) {
      throw new RuntimeException(message);
    }
  }

  private AdminBookingResponse mapToResponse(DatVe datVe) {
    List<Ve> veList = veRepository.findByDatVe_MaDatVe(datVe.getMaDatVe());
    Ve firstTicket = veList.isEmpty() ? null : veList.get(0);
    ChuyenXe chuyenXe = firstTicket != null ? firstTicket.getChuyenXe() : null;
    Xe xe = chuyenXe != null ? chuyenXe.getXe() : null;
    NhaXe nhaXe = xe != null ? xe.getNhaXe() : null;
    TuyenXe tuyenXe = chuyenXe != null ? chuyenXe.getTuyenXe() : null;
    BenXe benDi = tuyenXe != null ? tuyenXe.getBenDi() : null;
    BenXe benDen = tuyenXe != null ? tuyenXe.getBenDen() : null;
    KhachHang khachHang = datVe.getKhachHang();
    HoaDon hoaDon = hoaDonRepository.findByDatVe_MaDatVe(datVe.getMaDatVe()).orElse(null);
    ThanhToan thanhToan = hoaDon != null
            ? thanhToanRepository.findByHoaDon_MaHoaDon(hoaDon.getMaHoaDon()).orElse(null)
            : null;

    return new AdminBookingResponse(
            datVe.getMaDatVe(),
            datVe.getNgayDat(),
            datVe.getTrangThai(),
            khachHang != null ? khachHang.getMaKH() : null,
            khachHang != null ? khachHang.getTenKH() : null,
            khachHang != null ? khachHang.getEmail() : null,
            khachHang != null ? khachHang.getSdt() : null,
            chuyenXe != null ? chuyenXe.getMaChuyen() : null,
            nhaXe != null ? nhaXe.getTenNhaXe() : null,
            xe != null ? xe.getBienSo() : null,
            benDi != null ? benDi.getTenBen() : null,
            benDen != null ? benDen.getTenBen() : null,
            chuyenXe != null ? chuyenXe.getThoiGianKhoiHanh() : null,
            chuyenXe != null ? chuyenXe.getThoiGianDen() : null,
            veList.size(),
            veList.stream().map(Ve::getMaVe).toList(),
            veList.stream().map(this::getSeatNumber).toList(),
            hoaDon != null ? hoaDon.getGiaGoc() : BigDecimal.ZERO,
            hoaDon != null ? hoaDon.getTienGiam() : BigDecimal.ZERO,
            hoaDon != null ? hoaDon.getTongTien() : BigDecimal.ZERO,
            hoaDon != null ? hoaDon.getMaHoaDon() : null,
            hoaDon != null ? hoaDon.getTrangThai() : null,
            thanhToan != null ? thanhToan.getMaThanhToan() : null,
            thanhToan != null ? thanhToan.getOrderCode() : null,
            thanhToan != null ? thanhToan.getPhuongThucThanhToan() : null,
            thanhToan != null ? thanhToan.getTrangThai() : null,
            thanhToan != null ? thanhToan.getNgayThanhToan() : null
    );
  }

  private String getSeatNumber(Ve ve) {
    Ghe ghe = ve.getGhe();
    return ghe != null ? ghe.getSoGhe() : null;
  }
}
