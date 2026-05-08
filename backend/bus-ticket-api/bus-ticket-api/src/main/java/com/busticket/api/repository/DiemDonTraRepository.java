package com.busticket.api.repository;

import com.busticket.api.entity.DiemDonTra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiemDonTraRepository extends JpaRepository<DiemDonTra, String> {

  List<DiemDonTra> findByChuyenXe_MaChuyenOrderByThuTuAsc(String maChuyen);

  void deleteByChuyenXe_MaChuyen(String maChuyen);
}