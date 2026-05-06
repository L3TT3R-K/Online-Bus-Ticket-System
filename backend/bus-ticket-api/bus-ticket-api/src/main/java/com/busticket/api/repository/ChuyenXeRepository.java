package com.busticket.api.repository;

import com.busticket.api.dto.chuyenxe.ChuyenXeSearchProjection;
import com.busticket.api.entity.ChuyenXe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChuyenXeRepository extends JpaRepository<ChuyenXe, String> {

   @Query(value = """
            SELECT
                c.MACHUYEN AS "maChuyen",
                nx.TENNHAXE AS "tenNhaXe",
                x.BIENSO AS "bienSo",
                lx.TENLOAIXE AS "tenLoaiXe",
                bdi.TENBEN AS "diemDi",
                bden.TENBEN AS "diemDen",
                c.THOIGIANKHOIHANH AS "thoiGianKhoiHanh",
                c.THOIGIANDEN AS "thoiGianDen",
                c.GIAVE AS "giaVe",
                x.SOLUONGGHE AS "soLuongGhe",
                NVL(COUNT(v.MAVE), 0) AS "soGheDaDat",
                x.SOLUONGGHE - NVL(COUNT(v.MAVE), 0) AS "soGheTrong",
                c.TRANGTHAI AS "trangThai"
            FROM CHUYENXE c
            JOIN XE x ON c.MAXE = x.MAXE
            JOIN NHAXE nx ON x.MANHAXE = nx.MANHAXE
            JOIN LOAIXE lx ON x.MALOAIXE = lx.MALOAIXE
            JOIN TUYENXE tx ON c.MATUYEN = tx.MATUYEN
            JOIN BENXE bdi ON tx.MABENDI = bdi.MABEN
            JOIN BENXE bden ON tx.MABENDEN = bden.MABEN
            LEFT JOIN VE v ON c.MACHUYEN = v.MACHUYEN
                AND v.TRANGTHAI IN ('Giữ chỗ', 'Đã đặt')
            WHERE LOWER(bdi.TENBEN) LIKE LOWER('%' || :diemDi || '%')
              AND LOWER(bden.TENBEN) LIKE LOWER('%' || :diemDen || '%')
              AND c.THOIGIANKHOIHANH >= :startTime
              AND c.THOIGIANKHOIHANH < :endTime
              AND c.TRANGTHAI = 'Sắp chạy'
            GROUP BY
                c.MACHUYEN,
                nx.TENNHAXE,
                x.BIENSO,
                lx.TENLOAIXE,
                bdi.TENBEN,
                bden.TENBEN,
                c.THOIGIANKHOIHANH,
                c.THOIGIANDEN,
                c.GIAVE,
                x.SOLUONGGHE,
                c.TRANGTHAI
            ORDER BY c.THOIGIANKHOIHANH ASC
            """, nativeQuery = true)
   List<ChuyenXeSearchProjection> searchChuyenXe(
           @Param("diemDi") String diemDi,
           @Param("diemDen") String diemDen,
           @Param("startTime") LocalDateTime startTime,
           @Param("endTime") LocalDateTime endTime
   );
}