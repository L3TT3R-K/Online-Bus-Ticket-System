package com.busticket.api.repository;

import com.busticket.api.dto.chuyenxe.ChuyenXeSearchProjection;
import com.busticket.api.entity.ChuyenXe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChuyenXeRepository extends JpaRepository<ChuyenXe, String> {

   List<ChuyenXe> findByXe_NhaXe_MaNhaXeOrderByThoiGianKhoiHanhDesc(String maNhaXe);

   long countBy();



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
            (
                SELECT LISTAGG(ha.URL, '||') WITHIN GROUP (ORDER BY ha.THUTU)
                FROM HINHANH ha
                WHERE ha.MAXE = x.MAXE
            ) AS "imageUrls",
            (
                SELECT LISTAGG(ti.TENTIENICH, '||') WITHIN GROUP (ORDER BY ti.TENTIENICH)
                FROM TIENICHXE tix
                JOIN TIENICH ti ON tix.MATIENICH = ti.MATIENICH
                WHERE tix.MAXE = x.MAXE
            ) AS "amenities",
            (
                SELECT NVL(ROUND(AVG(dg.SOSAO), 1), 0)
                FROM DANHGIA dg
                JOIN CHUYENXE cx2 ON dg.MACHUYEN = cx2.MACHUYEN
                JOIN XE x2 ON cx2.MAXE = x2.MAXE
                WHERE x2.MANHAXE = nx.MANHAXE
            ) AS "rating",
            (
                SELECT COUNT(dg.MADANHGIA)
                FROM DANHGIA dg
                JOIN CHUYENXE cx2 ON dg.MACHUYEN = cx2.MACHUYEN
                JOIN XE x2 ON cx2.MAXE = x2.MAXE
                WHERE x2.MANHAXE = nx.MANHAXE
            ) AS "reviewCount",
            c.TRANGTHAI AS "trangThai"
        FROM CHUYENXE c
        JOIN XE x ON c.MAXE = x.MAXE
        JOIN NHAXE nx ON x.MANHAXE = nx.MANHAXE
        JOIN LOAIXE lx ON x.MALOAIXE = lx.MALOAIXE
        JOIN TUYENXE tx ON c.MATUYEN = tx.MATUYEN
        JOIN BENXE bdi ON tx.MABENDI = bdi.MABEN
        JOIN BENXE bden ON tx.MABENDEN = bden.MABEN
        LEFT JOIN VE v ON c.MACHUYEN = v.MACHUYEN
            AND v.TRANGTHAI IN ('Giữ chỗ', 'Đã đặt', 'Đã thanh toán', 'Đã dùng')
        WHERE LOWER(bdi.TENBEN) LIKE LOWER('%' || :diemDi || '%')
          AND LOWER(bden.TENBEN) LIKE LOWER('%' || :diemDen || '%')
          AND c.THOIGIANKHOIHANH >= :startTime
          AND c.THOIGIANKHOIHANH < :endTime
          AND c.TRANGTHAI IN ('Sắp chạy', 'Đang mở bán')
        GROUP BY
            c.MACHUYEN,
            nx.MANHAXE,
            nx.TENNHAXE,
            x.MAXE,
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
            (
                SELECT LISTAGG(ha.URL, '||') WITHIN GROUP (ORDER BY ha.THUTU)
                FROM HINHANH ha
                WHERE ha.MAXE = x.MAXE
            ) AS "imageUrls",
            (
                SELECT LISTAGG(ti.TENTIENICH, '||') WITHIN GROUP (ORDER BY ti.TENTIENICH)
                FROM TIENICHXE tix
                JOIN TIENICH ti ON tix.MATIENICH = ti.MATIENICH
                WHERE tix.MAXE = x.MAXE
            ) AS "amenities",
            (
                SELECT NVL(ROUND(AVG(dg.SOSAO), 1), 0)
                FROM DANHGIA dg
                JOIN CHUYENXE cx2 ON dg.MACHUYEN = cx2.MACHUYEN
                JOIN XE x2 ON cx2.MAXE = x2.MAXE
                WHERE x2.MANHAXE = nx.MANHAXE
            ) AS "rating",
            (
                SELECT COUNT(dg.MADANHGIA)
                FROM DANHGIA dg
                JOIN CHUYENXE cx2 ON dg.MACHUYEN = cx2.MACHUYEN
                JOIN XE x2 ON cx2.MAXE = x2.MAXE
                WHERE x2.MANHAXE = nx.MANHAXE
            ) AS "reviewCount",
            c.TRANGTHAI AS "trangThai"
        FROM CHUYENXE c
        JOIN XE x ON c.MAXE = x.MAXE
        JOIN NHAXE nx ON x.MANHAXE = nx.MANHAXE
        JOIN LOAIXE lx ON x.MALOAIXE = lx.MALOAIXE
        JOIN TUYENXE tx ON c.MATUYEN = tx.MATUYEN
        JOIN BENXE bdi ON tx.MABENDI = bdi.MABEN
        JOIN BENXE bden ON tx.MABENDEN = bden.MABEN
        LEFT JOIN VE v ON c.MACHUYEN = v.MACHUYEN
            AND v.TRANGTHAI IN ('Giữ chỗ', 'Đã đặt', 'Đã thanh toán', 'Đã dùng')
        WHERE c.MACHUYEN = :maChuyen
        GROUP BY
            c.MACHUYEN,
            nx.MANHAXE,
            nx.TENNHAXE,
            x.MAXE,
            x.BIENSO,
            lx.TENLOAIXE,
            bdi.TENBEN,
            bden.TENBEN,
            c.THOIGIANKHOIHANH,
            c.THOIGIANDEN,
            c.GIAVE,
            x.SOLUONGGHE,
            c.TRANGTHAI
        """, nativeQuery = true)
   Optional<ChuyenXeSearchProjection> findChuyenXeDetailByMaChuyen(
           @Param("maChuyen") String maChuyen
   );
}
