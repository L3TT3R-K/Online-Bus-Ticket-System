package com.busticket.api.repository;

import com.busticket.api.entity.LoaiXe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoaiXeRepository extends JpaRepository<LoaiXe, String> {

  Optional<LoaiXe> findByTenLoaiXe(String tenLoaiXe);
}