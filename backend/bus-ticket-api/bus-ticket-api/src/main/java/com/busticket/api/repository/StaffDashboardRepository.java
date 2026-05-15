package com.busticket.api.repository;

import com.busticket.api.dto.staff.MonthlyRevenueProjection;
import com.busticket.api.dto.staff.StaffRecentTripProjection;
import com.busticket.api.entity.ChuyenXe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface StaffDashboardRepository extends JpaRepository<ChuyenXe, String> {

  @Query(value = """
            SELECT COUNT(*)
            FROM XE x
            WHERE x.MANHAXE = :maNhaXe
            """, nativeQuery = true)
  Long countXeByNhaXe(@Param("maNhaXe") String maNhaXe);

  @Query(value = """
            SELECT COUNT(*)
            FROM V_DANH_SACH_CHUYEN v
            WHERE v.MANHAXE = :maNhaXe
            """, nativeQuery = true)
  Long countChuyenByNhaXe(@Param("maNhaXe") String maNhaXe);

  @Query(value = """
            SELECT COUNT(*)
            FROM VE v
            JOIN CHUYENXE c ON v.MACHUYEN = c.MACHUYEN
            JOIN XE x ON c.MAXE = x.MAXE
            WHERE x.MANHAXE = :maNhaXe
              AND v.TRANGTHAI IN ('Đã đặt', 'Đã thanh toán')
            """, nativeQuery = true)
  Long countTicketSoldByNhaXe(@Param("maNhaXe") String maNhaXe);

  @Query(value = """
            SELECT NVL(SUM(c.GIAVE), 0)
            FROM VE v
            JOIN CHUYENXE c ON v.MACHUYEN = c.MACHUYEN
            JOIN XE x ON c.MAXE = x.MAXE
            WHERE x.MANHAXE = :maNhaXe
              AND v.TRANGTHAI IN ('Đã đặt', 'Đã thanh toán')
            """, nativeQuery = true)
  BigDecimal totalRevenueByNhaXe(@Param("maNhaXe") String maNhaXe);

  @Query(value = """
            SELECT *
            FROM (
                SELECT
                    v.MACHUYEN AS "maChuyen",
                    v.BIENSO AS "bienSo",
                    v.BENDI || ' - ' || v.BENDEN AS "tuyen",
                    v.THOIGIANKHOIHANH AS "thoiGianKhoiHanh",
                    v.GIAVE AS "giaVe",
                    v.SOGHETRONG AS "soGheTrong",
                    v.TRANGTHAI AS "trangThai"
                FROM V_DANH_SACH_CHUYEN v
                WHERE v.MANHAXE = :maNhaXe
                ORDER BY v.THOIGIANKHOIHANH DESC
            )
            WHERE ROWNUM <= 5
            """, nativeQuery = true)
  List<StaffRecentTripProjection> findRecentTripsByNhaXe(@Param("maNhaXe") String maNhaXe);

  @Query(value = """
        SELECT
            EXTRACT(MONTH FROM c.THOIGIANKHOIHANH) AS "monthNumber",
            NVL(SUM(c.GIAVE), 0) AS "revenue"
        FROM VE v
        JOIN CHUYENXE c ON v.MACHUYEN = c.MACHUYEN
        JOIN XE x ON c.MAXE = x.MAXE
        WHERE x.MANHAXE = :maNhaXe
          AND EXTRACT(YEAR FROM c.THOIGIANKHOIHANH) = :year
          AND v.TRANGTHAI IN ('Đã đặt', 'Đã thanh toán')
        GROUP BY EXTRACT(MONTH FROM c.THOIGIANKHOIHANH)
        ORDER BY EXTRACT(MONTH FROM c.THOIGIANKHOIHANH)
        """, nativeQuery = true)
  List<MonthlyRevenueProjection> getMonthlyRevenueByNhaXe(
          @Param("maNhaXe") String maNhaXe,
          @Param("year") Integer year
  );
}
