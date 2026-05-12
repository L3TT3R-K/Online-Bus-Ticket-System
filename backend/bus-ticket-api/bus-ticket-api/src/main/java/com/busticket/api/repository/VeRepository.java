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



  List<Ve> findByChuyenXe_MaChuyenAndTrangThaiIn(
          String maChuyen,
          List<String> trangThai
  );

  List<Ve> findByChuyenXe_MaChuyenOrderByThoiGianDatDesc(String maChuyen);

  List<Ve> findByChuyenXe_Xe_NhaXe_MaNhaXeOrderByThoiGianDatDesc(String maNhaXe);

  List<Ve> findByChuyenXe_Xe_NhaXe_MaNhaXeAndTrangThaiIn(
          String maNhaXe,
          List<String> trangThai
  );

  boolean existsByChuyenXe_MaChuyenAndGhe_MaGheAndTrangThaiIn(
          String maChuyen,
          String maGhe,
          List<String> trangThai
  );

  List<Ve> findByDatVe_MaDatVe(String maDatVe);

  List<Ve> findByKhachHang_MaKHOrderByThoiGianDatDesc(String maKH);

  List<Ve> findByKhachHang_SdtContainingOrderByThoiGianDatDesc(String sdt);
}
