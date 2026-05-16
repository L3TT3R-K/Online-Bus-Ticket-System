package com.busticket.api.repository;

import com.busticket.api.entity.BenXe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BenXeRepository extends JpaRepository<BenXe, String> {
  List<BenXe> findAllByOrderByTenBenAsc();

  boolean existsByTenBenIgnoreCase(String tenBen);
}
