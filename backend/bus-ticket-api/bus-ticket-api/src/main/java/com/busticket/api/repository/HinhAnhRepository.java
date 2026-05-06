package com.busticket.api.repository;

import com.busticket.api.entity.HinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HinhAnhRepository extends JpaRepository<HinhAnh, String> {
  void deleteByMaXe(String maXe);
}