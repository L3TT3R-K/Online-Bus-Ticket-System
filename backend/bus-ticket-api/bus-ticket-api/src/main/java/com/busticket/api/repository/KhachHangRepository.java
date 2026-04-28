package com.busticket.api.repository;

import com.busticket.api.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KhachHangRepository extends JpaRepository<KhachHang, String> {

    boolean existsByEmail(String email);

    boolean existsBySdt(String sdt);
}