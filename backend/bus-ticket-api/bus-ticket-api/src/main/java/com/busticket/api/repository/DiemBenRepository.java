package com.busticket.api.repository;

import com.busticket.api.entity.DiemBen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiemBenRepository extends JpaRepository<DiemBen, String> {

  List<DiemBen> findByBenXe_MaBenAndTrangThaiOrderByLoaiAscThuTuAsc(
          String maBen,
          String trangThai
  );

  List<DiemBen> findByBenXe_MaBenAndLoaiInAndTrangThaiOrderByLoaiAscThuTuAsc(
          String maBen,
          List<String> loai,
          String trangThai
  );

  List<DiemBen> findByBenXe_MaBenAndTrangThaiOrderByThuTuAsc(
          String maBen,
          String trangThai
  );

  List<DiemBen> findByBenXe_MaBenAndLoaiInAndTrangThaiOrderByThuTuAsc(
          String maBen,
          List<String> loai,
          String trangThai
  );

  
}
