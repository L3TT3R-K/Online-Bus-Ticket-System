package com.busticket.api.service;

import com.busticket.api.dto.ve.KhachHangVeResponse;
import com.busticket.api.entity.ChuyenXe;
import com.busticket.api.entity.DiemDonTra;
import com.busticket.api.entity.TuyenXe;
import com.busticket.api.entity.Ve;
import com.busticket.api.repository.KhachHangRepository;
import com.busticket.api.repository.VeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VeService {

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

  public List<KhachHangVeResponse> searchVe(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      throw new RuntimeException("Từ khóa tìm kiếm không được để trống.");
    }

    return veRepository.findByKhachHang_SdtContainingAndTrangThaiNotOrderByThoiGianDatDesc(keyword.trim(), "Đã hủy")
            .stream()
            .map(this::mapToResponse)
            .toList();
  }

  private KhachHangVeResponse mapToResponse(Ve ve) {
    ChuyenXe chuyenXe = ve.getChuyenXe();
    TuyenXe tuyenXe = chuyenXe != null ? chuyenXe.getTuyenXe() : null;
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
