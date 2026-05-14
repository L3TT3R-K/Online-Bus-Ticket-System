package com.busticket.api.repository;

import com.busticket.api.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, String> {

  List<KhuyenMai> findByNgayBatDauLessThanEqualAndNgayKetThucGreaterThanEqualOrderByNgayKetThucAsc(
          LocalDateTime startTime,
          LocalDateTime endTime
  );
  Optional<KhuyenMai> findByMaKhuyenMaiAndTrangThai(String maKhuyenMai, String trangThai);

  @Query("""
          SELECT km
          FROM KhuyenMai km
          WHERE km.trangThai = :trangThai
            AND (km.ngayBatDau IS NULL OR km.ngayBatDau <= :now)
            AND (km.ngayKetThuc IS NULL OR km.ngayKetThuc >= :now)
          ORDER BY km.ngayKetThuc ASC, km.maKhuyenMai ASC
          """)
  List<KhuyenMai> findActivePromotions(
          @Param("trangThai") String trangThai,
          @Param("now") LocalDateTime now
  );
}
