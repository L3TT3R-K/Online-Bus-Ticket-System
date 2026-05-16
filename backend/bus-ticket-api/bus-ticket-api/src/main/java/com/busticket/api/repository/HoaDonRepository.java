package com.busticket.api.repository;

import com.busticket.api.dto.admin.AdminTopCompanyProjection;
import com.busticket.api.dto.staff.MonthlyRevenueProjection;
import com.busticket.api.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, String> {

  Optional<HoaDon> findByDatVe_MaDatVe(String maDatVe);

  boolean existsByKhuyenMai_MaKhuyenMai(String maKhuyenMai);

  @Query("""
        SELECT COALESCE(SUM(h.tongTien), 0)
        FROM HoaDon h
        WHERE h.trangThai = 'Đã thanh toán'
      """)
  BigDecimal totalPaidRevenue();

  @Query(value = """
        SELECT
            EXTRACT(MONTH FROM h.NGAYLAP) AS "monthNumber",
            NVL(SUM(h.TONGTIEN), 0) AS "revenue"
        FROM HOADON h
        WHERE h.TRANGTHAI = 'Đã thanh toán'
          AND EXTRACT(YEAR FROM h.NGAYLAP) = :year
        GROUP BY EXTRACT(MONTH FROM h.NGAYLAP)
        ORDER BY EXTRACT(MONTH FROM h.NGAYLAP)
        """, nativeQuery = true)
  List<MonthlyRevenueProjection> getPaidRevenueMonthly(@Param("year") Integer year);

  @Query(value = """
        SELECT *
        FROM (
            SELECT
                v.MANHAXE AS "maNhaXe",
                v.TENNHAXE AS "tenNhaXe",
                v.TRIPCOUNT AS "tripCount",
                v.PAIDORDERCOUNT AS "paidOrderCount",
                v.REVENUE AS "revenue"
            FROM V_DOANH_THU_HANG_XE_TONG_HOP v
            ORDER BY v.REVENUE DESC, v.TENNHAXE ASC
        )
        WHERE ROWNUM <= :limit
        """, nativeQuery = true)
  List<AdminTopCompanyProjection> findTopCompaniesByRevenue(@Param("limit") Integer limit);

  @Query(value = """
        SELECT
            v.MANHAXE AS "maNhaXe",
            v.TENNHAXE AS "tenNhaXe",
            v.TRIPCOUNT AS "tripCount",
            v.PAIDORDERCOUNT AS "paidOrderCount",
            v.REVENUE AS "revenue"
        FROM V_DOANH_THU_HANG_XE_TONG_HOP v
        ORDER BY v.REVENUE DESC, v.TENNHAXE ASC
        """, nativeQuery = true)
  List<AdminTopCompanyProjection> findCompanyRevenueReport();
}
