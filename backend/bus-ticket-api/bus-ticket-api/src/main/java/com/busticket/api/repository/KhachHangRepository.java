package com.busticket.api.repository;

import com.busticket.api.entity.KhachHang;
import com.busticket.api.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, String> {

    boolean existsByEmail(String email);

    boolean existsBySdt(String sdt);

    Optional<KhachHang> findByTaiKhoan(TaiKhoan taiKhoan);
}
