package com.busticket.api.service;

import com.busticket.api.dto.StaffDashboardResponse;
import com.busticket.api.dto.StaffRecentTripProjection;
import com.busticket.api.dto.StaffRecentTripResponse;
import com.busticket.api.entity.NhanVien;
import com.busticket.api.entity.TaiKhoan;
import com.busticket.api.repository.NhanVienRepository;
import com.busticket.api.repository.StaffDashboardRepository;
import com.busticket.api.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffDashboardService {

  private final TaiKhoanRepository taiKhoanRepository;
  private final NhanVienRepository nhanVienRepository;
  private final StaffDashboardRepository staffDashboardRepository;

  public StaffDashboardResponse getDashboard(Long maTK) {
    TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

    if (!"NhanVien".equals(taiKhoan.getQuyen())) {
      throw new RuntimeException("Tài khoản này không phải nhân viên nhà xe");
    }

    NhanVien nhanVien = nhanVienRepository.findByTaiKhoan(taiKhoan)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();
    String tenNhaXe = nhanVien.getNhaXe().getTenNhaXe();

    Long totalBus = staffDashboardRepository.countXeByNhaXe(maNhaXe);
    Long totalTrip = staffDashboardRepository.countChuyenByNhaXe(maNhaXe);
    Long totalTicket = staffDashboardRepository.countTicketSoldByNhaXe(maNhaXe);
    BigDecimal totalRevenue = staffDashboardRepository.totalRevenueByNhaXe(maNhaXe);

    List<StaffRecentTripProjection> recentTripProjections =
            staffDashboardRepository.findRecentTripsByNhaXe(maNhaXe);

    List<StaffRecentTripResponse> recentTrips = recentTripProjections.stream()
            .map(item -> new StaffRecentTripResponse(
                    item.getMaChuyen(),
                    item.getBienSo(),
                    item.getTuyen(),
                    item.getThoiGianKhoiHanh(),
                    item.getGiaVe(),
                    item.getSoGheTrong(),
                    item.getTrangThai()
            ))
            .toList();

    return new StaffDashboardResponse(
            true,
            "Lấy dữ liệu tổng quan thành công",
            maNhaXe,
            tenNhaXe,
            totalBus,
            totalTrip,
            totalTicket,
            totalRevenue,
            recentTrips
    );
  }
}