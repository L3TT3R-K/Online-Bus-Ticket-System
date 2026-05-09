package com.busticket.api.repository;

import com.busticket.api.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, String> {

  List<KhuyenMai> findByNgayBatDauLessThanEqualAndNgayKetThucGreaterThanEqualOrderByNgayKetThucAsc(
          LocalDateTime startTime,
          LocalDateTime endTime
  );
}
