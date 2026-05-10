package com.busticket.api.repository;

import com.busticket.api.entity.DiemDonTra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiemDonTraRepository extends JpaRepository<DiemDonTra, String> {

  List<DiemDonTra> findByChuyenXe_MaChuyenOrderByLoaiAscThuTuAsc(String maChuyen);

  List<DiemDonTra> findByChuyenXe_MaChuyenAndLoaiOrderByThuTuAsc(
          String maChuyen,
          String loai
  );

  void deleteByChuyenXe_MaChuyen(String maChuyen);
}