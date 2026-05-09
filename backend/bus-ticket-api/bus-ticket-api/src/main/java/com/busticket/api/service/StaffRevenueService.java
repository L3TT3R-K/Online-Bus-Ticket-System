package com.busticket.api.service;

import com.busticket.api.dto.staffxe.StaffRevenueSummaryResponse;
import com.busticket.api.dto.staffxe.StaffRevenueTripResponse;
import com.busticket.api.entity.ChuyenXe;
import com.busticket.api.entity.NhanVien;
import com.busticket.api.entity.TaiKhoan;
import com.busticket.api.entity.Ve;
import com.busticket.api.repository.ChuyenXeRepository;
import com.busticket.api.repository.NhanVienRepository;
import com.busticket.api.repository.TaiKhoanRepository;
import com.busticket.api.repository.VeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StaffRevenueService {

  private final TaiKhoanRepository taiKhoanRepository;
  private final NhanVienRepository nhanVienRepository;
  private final VeRepository veRepository;

  private final ChuyenXeRepository chuyenXeRepository;

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

  public List<StaffRevenueTripResponse> getRevenueByTrips(Long maTK) {
    NhanVien nhanVien = getNhanVienFromMaTK(maTK);

    String maNhaXe = nhanVien.getNhaXe().getMaNhaXe();

    List<ChuyenXe> chuyenXeList =
            chuyenXeRepository.findByXe_NhaXe_MaNhaXeOrderByThoiGianKhoiHanhDesc(maNhaXe);

    List<String> paidStatuses = List.of(
            "Đã đặt",
            "Đã thanh toán",
            "Đã dùng"
    );

    List<Ve> paidTickets =
            veRepository.findByChuyenXe_Xe_NhaXe_MaNhaXeAndTrangThaiIn(maNhaXe, paidStatuses);

    Map<String, BigDecimal> revenueByTrip = new HashMap<>();
    Map<String, Long> ticketCountByTrip = new HashMap<>();

    for (Ve ve : paidTickets) {
      if (ve.getChuyenXe() == null || ve.getChuyenXe().getMaChuyen() == null) {
        continue;
      }

      String maChuyen = ve.getChuyenXe().getMaChuyen();

      BigDecimal giaTien = ve.getGiaTien() != null
              ? ve.getGiaTien()
              : BigDecimal.ZERO;

      revenueByTrip.put(
              maChuyen,
              revenueByTrip.getOrDefault(maChuyen, BigDecimal.ZERO).add(giaTien)
      );

      ticketCountByTrip.put(
              maChuyen,
              ticketCountByTrip.getOrDefault(maChuyen, 0L) + 1
      );
    }

    return chuyenXeList.stream()
            .map(chuyenXe -> {
              String maChuyen = chuyenXe.getMaChuyen();

              BigDecimal doanhThu = revenueByTrip.getOrDefault(maChuyen, BigDecimal.ZERO);
              Long soVeDaThanhToan = ticketCountByTrip.getOrDefault(maChuyen, 0L);

              Integer tongGhe = chuyenXe.getXe() != null && chuyenXe.getXe().getSoLuongGhe() != null
                      ? chuyenXe.getXe().getSoLuongGhe()
                      : 0;

              int tyLeLapDay = 0;

              if (tongGhe > 0) {
                tyLeLapDay = Math.min(
                        100,
                        Math.round((soVeDaThanhToan.floatValue() / tongGhe) * 100)
                );
              }

              String tenTuyen = buildTenTuyen(chuyenXe);

              LocalDateTime thoiGianKhoiHanh = chuyenXe.getThoiGianKhoiHanh();

              return new StaffRevenueTripResponse(
                      chuyenXe.getMaChuyen(),
                      tenTuyen,
                      chuyenXe.getXe() != null ? chuyenXe.getXe().getMaXe() : null,
                      chuyenXe.getXe() != null ? chuyenXe.getXe().getBienSo() : "Không rõ xe",
                      thoiGianKhoiHanh != null ? thoiGianKhoiHanh.toLocalDate() : null,
                      thoiGianKhoiHanh != null ? thoiGianKhoiHanh.toLocalTime() : null,
                      tongGhe,
                      soVeDaThanhToan,
                      tyLeLapDay,
                      doanhThu
              );
            })
            .toList();
  }

  private String buildTenTuyen(ChuyenXe chuyenXe) {
    if (chuyenXe.getTuyenXe() == null) {
      return "Chưa có tuyến";
    }

    if (chuyenXe.getTuyenXe().getBenDi() == null || chuyenXe.getTuyenXe().getBenDen() == null) {
      return "Chưa có tuyến";
    }

    return chuyenXe.getTuyenXe().getBenDi().getTenBen()
            + " - "
            + chuyenXe.getTuyenXe().getBenDen().getTenBen();
  }
}