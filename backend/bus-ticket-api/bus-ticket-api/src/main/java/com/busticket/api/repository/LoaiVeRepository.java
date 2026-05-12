package com.busticket.api.repository;

import com.busticket.api.entity.LoaiVe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoaiVeRepository extends JpaRepository<LoaiVe, String> {

  Optional<LoaiVe> findFirstByTrangThaiOrderByMaLoaiVeAsc(String trangThai);
}
