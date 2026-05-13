package com.busticket.api.service;

import com.busticket.api.dto.admin.AdminDashboardSummaryResponse;
import com.busticket.api.dto.admin.AdminTopCompanyProjection;
import com.busticket.api.dto.admin.AdminTopCompanyResponse;
import com.busticket.api.dto.staff.MonthlyRevenueProjection;
import com.busticket.api.dto.staff.MonthlyRevenueResponse;
import com.busticket.api.repository.ChuyenXeRepository;
import com.busticket.api.repository.HoaDonRepository;
import com.busticket.api.repository.KhachHangRepository;
import com.busticket.api.repository.NhaXeRepository;
import com.busticket.api.repository.TaiKhoanRepository;
import com.busticket.api.repository.VeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

  private final TaiKhoanRepository taiKhoanRepository;
  private final KhachHangRepository khachHangRepository;
  private final NhaXeRepository nhaXeRepository;
  private final ChuyenXeRepository chuyenXeRepository;
  private final VeRepository veRepository;
  private final HoaDonRepository hoaDonRepository;

  public AdminDashboardSummaryResponse getSummary() {
    return new AdminDashboardSummaryResponse(
            taiKhoanRepository.count(),
            khachHangRepository.count(),
            nhaXeRepository.count(),
            chuyenXeRepository.count(),
            veRepository.count(),
            safeRevenue(hoaDonRepository.totalPaidRevenue())
    );
  }

  public List<MonthlyRevenueResponse> getRevenueMonthly(Integer year) {
    Integer targetYear = year != null ? year : LocalDate.now().getYear();

    List<MonthlyRevenueProjection> data =
            hoaDonRepository.getPaidRevenueMonthly(targetYear);

    Map<Integer, BigDecimal> revenueMap = data.stream()
            .collect(Collectors.toMap(
                    MonthlyRevenueProjection::getMonthNumber,
                    MonthlyRevenueProjection::getRevenue
            ));

    List<MonthlyRevenueResponse> result = new ArrayList<>();
    for (int month = 1; month <= 12; month++) {
      result.add(new MonthlyRevenueResponse(
              "T" + month,
              month,
              revenueMap.getOrDefault(month, BigDecimal.ZERO)
      ));
    }

    return result;
  }

  public List<AdminTopCompanyResponse> getTopCompanies(Integer limit) {
    int resolvedLimit = resolveLimit(limit);

    return hoaDonRepository.findTopCompaniesByRevenue(resolvedLimit)
            .stream()
            .map(this::mapTopCompany)
            .toList();
  }

  public List<AdminTopCompanyResponse> getCompanyRevenueReport() {
    return hoaDonRepository.findCompanyRevenueReport()
            .stream()
            .map(this::mapTopCompany)
            .toList();
  }

  private AdminTopCompanyResponse mapTopCompany(AdminTopCompanyProjection item) {
    return new AdminTopCompanyResponse(
            item.getMaNhaXe(),
            item.getTenNhaXe(),
            item.getTripCount(),
            item.getPaidOrderCount(),
            safeRevenue(item.getRevenue())
    );
  }

  private int resolveLimit(Integer limit) {
    if (limit == null) {
      return 5;
    }

    if (limit < 1 || limit > 50) {
      throw new RuntimeException("Giới hạn top nhà xe phải nằm trong khoảng từ 1 đến 50.");
    }

    return limit;
  }

  private BigDecimal safeRevenue(BigDecimal revenue) {
    return revenue != null ? revenue : BigDecimal.ZERO;
  }
}
