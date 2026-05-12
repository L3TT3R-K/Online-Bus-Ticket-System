package com.busticket.api.repository;

import com.busticket.api.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DanhGiaRepository extends JpaRepository<DanhGia, String> {

  boolean existsByKhachHang_MaKHAndChuyenXe_MaChuyen(String maKhachHang, String maChuyen);
}
