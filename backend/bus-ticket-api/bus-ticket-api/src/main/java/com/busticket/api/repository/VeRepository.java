package com.busticket.api.repository;

import com.busticket.api.entity.Ve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VeRepository extends JpaRepository<Ve, String> {

  @Query("""
        SELECT COUNT(v)
        FROM Ve v
        WHERE v.chuyenXe.maChuyen = :maChuyen
          AND v.trangThai IN ('Đã đặt', 'Đã thanh toán')
    """)
  long countBookedSeatsByMaChuyen(@Param("maChuyen") String maChuyen);
}