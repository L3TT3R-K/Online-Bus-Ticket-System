package com.busticket.api.repository;

import com.busticket.api.dto.staff.MonthlyRevenueProjection;
import com.busticket.api.dto.admin.AdminTopCompanyProjection;
import com.busticket.api.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, String> {

  Optional<HoaDon> findByDatVe_MaDatVe(String maDatVe);

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
                nx.MANHAXE AS "maNhaXe",
                nx.TENNHAXE AS "tenNhaXe",
                COUNT(DISTINCT c.MACHUYEN) AS "tripCount",
                COUNT(DISTINCT h.MAHOADON) AS "paidOrderCount",
                NVL(SUM(h.TONGTIEN), 0) AS "revenue"
            FROM HOADON h
            JOIN DATVE dv ON h.MADATVE = dv.MADATVE
            JOIN VE v ON dv.MADATVE = v.MADATVE
            JOIN CHUYENXE c ON v.MACHUYEN = c.MACHUYEN
            JOIN XE x ON c.MAXE = x.MAXE
            JOIN NHAXE nx ON x.MANHAXE = nx.MANHAXE
            WHERE h.TRANGTHAI = 'Đã thanh toán'
            GROUP BY nx.MANHAXE, nx.TENNHAXE
            ORDER BY NVL(SUM(h.TONGTIEN), 0) DESC, nx.TENNHAXE ASC
        )
        WHERE ROWNUM <= :limit
        """, nativeQuery = true)
  List<AdminTopCompanyProjection> findTopCompaniesByRevenue(@Param("limit") Integer limit);

  @Query(value = """
        SELECT
            nx.MANHAXE AS "maNhaXe",
            nx.TENNHAXE AS "tenNhaXe",
            COUNT(DISTINCT c.MACHUYEN) AS "tripCount",
            COUNT(DISTINCT h.MAHOADON) AS "paidOrderCount",
            NVL(SUM(h.TONGTIEN), 0) AS "revenue"
        FROM NHAXE nx
        LEFT JOIN XE x ON nx.MANHAXE = x.MANHAXE
        LEFT JOIN CHUYENXE c ON x.MAXE = c.MAXE
        LEFT JOIN VE v ON c.MACHUYEN = v.MACHUYEN
        LEFT JOIN DATVE dv ON v.MADATVE = dv.MADATVE
        LEFT JOIN HOADON h ON dv.MADATVE = h.MADATVE
            AND h.TRANGTHAI = 'Đã thanh toán'
        GROUP BY nx.MANHAXE, nx.TENNHAXE
        ORDER BY NVL(SUM(h.TONGTIEN), 0) DESC, nx.TENNHAXE ASC
        """, nativeQuery = true)
  List<AdminTopCompanyProjection> findCompanyRevenueReport();
}
