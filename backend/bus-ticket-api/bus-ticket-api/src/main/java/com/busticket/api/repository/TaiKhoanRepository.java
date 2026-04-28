package com.busticket.api.repository;

import com.busticket.api.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Long> {

    boolean existsByTenDangNhap(String tenDangNhap);
}