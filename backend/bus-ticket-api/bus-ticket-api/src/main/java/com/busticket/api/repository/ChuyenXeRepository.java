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
            v.MACHUYEN AS "maChuyen",
            v.TENNHAXE AS "tenNhaXe",
            v.BIENSO AS "bienSo",
            v.TENLOAIXE AS "tenLoaiXe",
            v.BENDI AS "diemDi",
            v.BENDEN AS "diemDen",
            v.THOIGIANKHOIHANH AS "thoiGianKhoiHanh",
            v.THOIGIANDEN AS "thoiGianDen",
            v.GIAVE AS "giaVe",
            v.SOLUONGGHE AS "soLuongGhe",
            v.SOGHEDADAT AS "soGheDaDat",
            v.SOGHETRONG AS "soGheTrong",
            v.IMAGEURLS AS "imageUrls",
            v.AMENITIES AS "amenities",
            v.RATING AS "rating",
            v.REVIEWCOUNT AS "reviewCount",
            v.TRANGTHAI AS "trangThai"
        FROM V_DANH_SACH_CHUYEN v
        WHERE LOWER(v.BENDI) LIKE LOWER('%' || :diemDi || '%')
          AND LOWER(v.BENDEN) LIKE LOWER('%' || :diemDen || '%')
          AND v.THOIGIANKHOIHANH >= :startTime
          AND v.THOIGIANKHOIHANH < :endTime
          AND v.TRANGTHAI IN ('Sắp chạy', 'Đang mở bán')
        ORDER BY v.THOIGIANKHOIHANH ASC
        """, nativeQuery = true)
   List<ChuyenXeSearchProjection> searchChuyenXe(
           @Param("diemDi") String diemDi,
           @Param("diemDen") String diemDen,
           @Param("startTime") LocalDateTime startTime,
           @Param("endTime") LocalDateTime endTime
   );

   @Query(value = """
        SELECT
            v.MACHUYEN AS "maChuyen",
            v.TENNHAXE AS "tenNhaXe",
            v.BIENSO AS "bienSo",
            v.TENLOAIXE AS "tenLoaiXe",
            v.BENDI AS "diemDi",
            v.BENDEN AS "diemDen",
            v.THOIGIANKHOIHANH AS "thoiGianKhoiHanh",
            v.THOIGIANDEN AS "thoiGianDen",
            v.GIAVE AS "giaVe",
            v.SOLUONGGHE AS "soLuongGhe",
            v.SOGHEDADAT AS "soGheDaDat",
            v.SOGHETRONG AS "soGheTrong",
            v.IMAGEURLS AS "imageUrls",
            v.AMENITIES AS "amenities",
            v.RATING AS "rating",
            v.REVIEWCOUNT AS "reviewCount",
            v.TRANGTHAI AS "trangThai"
        FROM V_DANH_SACH_CHUYEN v
        WHERE v.MACHUYEN = :maChuyen
        """, nativeQuery = true)
   Optional<ChuyenXeSearchProjection> findChuyenXeDetailByMaChuyen(
           @Param("maChuyen") String maChuyen
   );
}
