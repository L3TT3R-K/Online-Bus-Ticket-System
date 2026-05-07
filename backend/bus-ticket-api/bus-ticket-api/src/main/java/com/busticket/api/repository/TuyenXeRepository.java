package com.busticket.api.repository;

import com.busticket.api.entity.TuyenXe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TuyenXeRepository extends JpaRepository<TuyenXe, String> {

  Optional<TuyenXe> findByBenDi_MaBenAndBenDen_MaBen(String maBenDi, String maBenDen);
}