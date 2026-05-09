package com.busticket.api.service;

import com.busticket.api.dto.staffxe.StaffRevenueSummaryResponse;
import com.busticket.api.entity.NhanVien;
import com.busticket.api.entity.TaiKhoan;
import com.busticket.api.entity.Ve;
import com.busticket.api.repository.NhanVienRepository;
import com.busticket.api.repository.TaiKhoanRepository;
import com.busticket.api.repository.VeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StaffRevenueService {

  private final TaiKhoanRepository taiKhoanRepository;
  private final NhanVienRepository nhanVienRepository;
  private final VeRepository veRepository;

  public StaffRevenueSummaryResponse getRevenueSummary(Long maTK) {
    NhanVien nhanVien = getNhanVienFromMaTK(maTK);

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();

    List<String> paidStatuses = List.of(
            "Đã đặt",
            "Đã thanh toán",
            "Đã dùng"
    );

    List<Ve> paidTickets = veRepository.findByChuyenXe_Xe_NhaXe_MaNhaXeAndTrangThaiIn(
            maNhaXe,
            paidStatuses
    );

    BigDecimal totalRevenue = BigDecimal.ZERO;
    long paidTicketCount = paidTickets.size();

    Map<String, BigDecimal> revenueByTrip = new HashMap<>();

    for (Ve ve : paidTickets) {
      BigDecimal giaTien = ve.getGiaTien() != null
              ? ve.getGiaTien()
              : BigDecimal.ZERO;

      totalRevenue = totalRevenue.add(giaTien);

      if (ve.getChuyenXe() != null && ve.getChuyenXe().getMaChuyen() != null) {
        String maChuyen = ve.getChuyenXe().getMaChuyen();

        revenueByTrip.put(
                maChuyen,
                revenueByTrip.getOrDefault(maChuyen, BigDecimal.ZERO).add(giaTien)
        );
      }
    }

    String topTripId = null;
    BigDecimal topTripRevenue = BigDecimal.ZERO;

    for (Map.Entry<String, BigDecimal> entry : revenueByTrip.entrySet()) {
      if (entry.getValue().compareTo(topTripRevenue) > 0) {
        topTripId = entry.getKey();
        topTripRevenue = entry.getValue();
      }
    }

    return new StaffRevenueSummaryResponse(
            totalRevenue,
            paidTicketCount,
            topTripId,
            topTripRevenue
    );
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
}