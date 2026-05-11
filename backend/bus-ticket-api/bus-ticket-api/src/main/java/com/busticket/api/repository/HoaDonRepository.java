package com.busticket.api.repository;

import com.busticket.api.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, String> {

  Optional<HoaDon> findByDatVe_MaDatVe(String maDatVe);
}