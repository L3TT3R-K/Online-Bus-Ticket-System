package com.busticket.api.repository;

import com.busticket.api.entity.Ve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface VeRepository extends JpaRepository<Ve, String> {

  @Query("""
        SELECT COUNT(v)
        FROM Ve v
        WHERE v.chuyenXe.maChuyen = :maChuyen
          AND v.trangThai IN ('Đã đặt', 'Đã thanh toán')
    """)
  long countBookedSeatsByMaChuyen(@Param("maChuyen") String maChuyen);



  List<Ve> findByChuyenXe_Xe_NhaXe_MaNhaXeOrderByThoiGianDatDesc(String maNhaXe);

  boolean existsByChuyenXe_MaChuyenAndSoGheAndTrangThaiIn(
          String maChuyen,
          String soGhe,
          List<String> trangThai
  );

  List<Ve> findByChuyenXe_Xe_NhaXe_MaNhaXeAndTrangThaiIn(
          String maNhaXe,
          List<String> trangThai
  );
  List<Ve> findByChuyenXe_MaChuyenAndTrangThaiIn(
          String maChuyen,
          List<String> trangThai
  );
}