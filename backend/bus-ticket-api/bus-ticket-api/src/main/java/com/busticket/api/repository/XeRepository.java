package com.busticket.api.repository;

import com.busticket.api.entity.Xe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface XeRepository extends JpaRepository<Xe, String> {

  Optional<Xe> findByMaXeAndNhaXe_MaNhaXe(String maXe, String maNhaXe);

  boolean existsByLoaiXe_MaLoaiXe(String maLoaiXe);
}
