package com.busticket.api.service;

import com.busticket.api.dto.staffxe.StaffVeResponse;
import com.busticket.api.entity.*;
import com.busticket.api.repository.NhanVienRepository;
import com.busticket.api.repository.TaiKhoanRepository;
import com.busticket.api.repository.VeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffVeService {

  private final TaiKhoanRepository taiKhoanRepository;
  private final NhanVienRepository nhanVienRepository;
  private final VeRepository veRepository;

  public List<StaffVeResponse> getVeCuaNhaXe(Long maTK) {
    NhanVien nhanVien = getNhanVienFromMaTK(maTK);

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();

    return veRepository
            .findByChuyenXe_Xe_NhaXe_MaNhaXeOrderByThoiGianDatDesc(maNhaXe)
            .stream()
            .map(this::mapToResponse)
            .toList();
  }

  private NhanVien getNhanVienFromMaTK(Long maTK) {
    TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản."));

    if (!"NhanVien".equals(taiKhoan.getQuyen())) {
      throw new RuntimeException("Tài khoản này không phải nhân viên nhà xe.");
    }

    if (!"Hoạt động".equals(taiKhoan.getTrangThaiTK())) {
      throw new RuntimeException("Tài khoản đã bị khóa.");
    }

    return nhanVienRepository.findByTaiKhoan(taiKhoan)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên."));
  }

  private StaffVeResponse mapToResponse(Ve ve) {
    ChuyenXe chuyenXe = ve.getChuyenXe();
    TuyenXe tuyenXe = chuyenXe != null ? chuyenXe.getTuyenXe() : null;
    KhachHang khachHang = ve.getKhachHang();

    String tenTuyen = "";
    if (tuyenXe != null && tuyenXe.getBenDi() != null && tuyenXe.getBenDen() != null) {
      tenTuyen = tuyenXe.getBenDi().getTenBen() + " - " + tuyenXe.getBenDen().getTenBen();
    }

    BigDecimal giaTien = ve.getGiaTien() != null
            ? ve.getGiaTien()
            : BigDecimal.ZERO;

    String trangThaiThanhToan = mapTrangThaiThanhToan(ve.getTrangThai());

    return new StaffVeResponse(
            ve.getMaVe(),

            chuyenXe != null ? chuyenXe.getMaChuyen() : null,
            tenTuyen,
            chuyenXe != null ? chuyenXe.getThoiGianKhoiHanh() : null,

            getSoGhe(ve),

            khachHang != null ? khachHang.getMaKH() : null,
            khachHang != null ? khachHang.getTenKH() : "Khách vãng lai",
            khachHang != null ? khachHang.getSdt() : "",

            giaTien,

            ve.getThoiGianDat(),

            ve.getTrangThai(),
            trangThaiThanhToan
    );
  }

  private String mapTrangThaiThanhToan(String trangThaiVe) {
    if (trangThaiVe == null) {
      return "Chờ thanh toán";
    }

    return switch (trangThaiVe) {
      case "Đã đặt", "Đã dùng" -> "Đã thanh toán";
      case "Giữ chỗ" -> "Chờ thanh toán";
      case "Đã hủy" -> "Đã hủy";
      default -> "Chờ thanh toán";
    };
  }

  private String getSoGhe(Ve ve) {
    if (ve == null || ve.getGhe() == null) {
      return null;
    }

    return ve.getGhe().getSoGhe();
  }
}
