package com.busticket.api.repository;

import com.busticket.api.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, String> {

  Optional<ThanhToan> findByOrderCode(Long orderCode);

  Optional<ThanhToan> findByHoaDon_MaHoaDon(String maHoaDon);

  boolean existsByOrderCode(Long orderCode);
}