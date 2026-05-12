package com.busticket.api.service;

import com.busticket.api.dto.ve.KhachHangVeResponse;
import com.busticket.api.dto.ve.UpdateVeStatusRequest;
import com.busticket.api.entity.ChuyenXe;
import com.busticket.api.entity.DiemDonTra;
import com.busticket.api.entity.NhaXe;
import com.busticket.api.entity.TuyenXe;
import com.busticket.api.entity.Ve;
import com.busticket.api.entity.Xe;
import com.busticket.api.repository.KhachHangRepository;
import com.busticket.api.repository.VeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VeService {

  private static final List<String> VALID_TICKET_STATUSES = List.of(
          "Giữ chỗ",
          "Đã đặt",
          "Đã thanh toán",
          "Đã hủy",
          "Đã dùng"
  );

  private final KhachHangRepository khachHangRepository;
  private final VeRepository veRepository;

  public List<KhachHangVeResponse> getVeByKhachHang(String maKhachHang) {
    if (maKhachHang == null || maKhachHang.isBlank()) {
      throw new RuntimeException("Mã khách hàng không được để trống.");
    }

    String maKH = maKhachHang.trim();
    if (!khachHangRepository.existsById(maKH)) {
      throw new RuntimeException("Không tìm thấy khách hàng.");
    }

    return veRepository.findByKhachHang_MaKHAndTrangThaiNotOrderByThoiGianDatDesc(maKH, "Đã hủy")
            .stream()
            .map(this::mapToResponse)
            .toList();
  }

  @Transactional
  public KhachHangVeResponse updateStatus(String maVe, UpdateVeStatusRequest request) {
    if (maVe == null || maVe.isBlank()) {
      throw new RuntimeException("Mã vé không được để trống.");
    }

    if (request == null || request.getTrangThai() == null || request.getTrangThai().isBlank()) {
      throw new RuntimeException("Trạng thái vé không được để trống.");
    }

    String trangThai = request.getTrangThai().trim();
    if (!VALID_TICKET_STATUSES.contains(trangThai)) {
      throw new RuntimeException("Trạng thái vé không hợp lệ.");
    }

    Ve ve = veRepository.findById(maVe.trim())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy vé."));

    ve.setTrangThai(trangThai);
    Ve savedVe = veRepository.save(ve);

    return mapToResponse(savedVe);
  }

  private KhachHangVeResponse mapToResponse(Ve ve) {
    ChuyenXe chuyenXe = ve.getChuyenXe();
    TuyenXe tuyenXe = chuyenXe != null ? chuyenXe.getTuyenXe() : null;
    Xe xe = chuyenXe != null ? chuyenXe.getXe() : null;
    NhaXe nhaXe = xe != null ? xe.getNhaXe() : null;
    DiemDonTra diemDon = ve.getDiemDon();
    DiemDonTra diemTra = ve.getDiemTra();

    String tenTuyen = "";
    if (tuyenXe != null && tuyenXe.getBenDi() != null && tuyenXe.getBenDen() != null) {
      tenTuyen = tuyenXe.getBenDi().getTenBen() + " - " + tuyenXe.getBenDen().getTenBen();
    }

    return new KhachHangVeResponse(
            ve.getMaVe(),
            ve.getDatVe() != null ? ve.getDatVe().getMaDatVe() : null,
            ve.getKhachHang() != null ? ve.getKhachHang().getTenKH() : null,
            chuyenXe != null ? chuyenXe.getMaChuyen() : null,
            tenTuyen,
            chuyenXe != null ? chuyenXe.getThoiGianKhoiHanh() : null,
            chuyenXe != null ? chuyenXe.getThoiGianDen() : null,
            nhaXe != null ? nhaXe.getMaNhaXe() : null,
            nhaXe != null ? nhaXe.getTenNhaXe() : null,
            ve.getGhe() != null ? ve.getGhe().getSoGhe() : null,
            diemDon != null ? diemDon.getMaDiem() : null,
            diemDon != null ? diemDon.getTenDiem() : null,
            diemTra != null ? diemTra.getMaDiem() : null,
            diemTra != null ? diemTra.getTenDiem() : null,
            ve.getGiaTien() != null ? ve.getGiaTien() : BigDecimal.ZERO,
            ve.getThoiGianDat(),
            ve.getTrangThai()
    );
  }
}
