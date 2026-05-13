package com.busticket.api.service;

import com.busticket.api.dto.admin.AdminBookingResponse;
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

@Service
@RequiredArgsConstructor
public class AdminBookingService {

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

    List<Ve> veCungDon = veRepository.findByDatVe_MaDatVe(datVe.getMaDatVe());
    boolean allCanceled = veCungDon.stream()
            .allMatch(item -> "Đã hủy".equals(item.getTrangThai()));

    if (allCanceled) {
      datVe.setTrangThai("Đã hủy");
    } else {
      datVe.setTrangThai("Đã hủy một phần");
    }
    DatVe savedDatVe = datVeRepository.save(datVe);

    return mapToResponse(savedDatVe);
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
