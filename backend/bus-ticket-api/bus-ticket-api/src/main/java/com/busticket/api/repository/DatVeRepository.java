package com.busticket.api.repository;

import com.busticket.api.entity.DatVe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatVeRepository extends JpaRepository<DatVe, String> {

  List<DatVe> findByKhachHang_MaKHOrderByNgayDatDesc(String maKH);
}