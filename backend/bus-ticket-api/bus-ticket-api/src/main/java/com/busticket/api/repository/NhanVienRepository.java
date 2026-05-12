package com.busticket.api.repository;

import com.busticket.api.entity.NhanVien;
import com.busticket.api.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, String> {

  Optional<NhanVien> findByTaiKhoan(TaiKhoan taiKhoan);

  Optional<NhanVien> findByTaiKhoan_MaTK(Integer maTK);

  boolean existsByEmail(String email);

  boolean existsBySdt(String sdt);
}
