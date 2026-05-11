package com.busticket.api.repository;

import com.busticket.api.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, String> {

  List<KhuyenMai> findByNgayBatDauLessThanEqualAndNgayKetThucGreaterThanEqualOrderByNgayKetThucAsc(
          LocalDateTime startTime,
          LocalDateTime endTime
  );
  Optional<KhuyenMai> findByMaKhuyenMaiAndTrangThai(String maKhuyenMai, String trangThai);
}
