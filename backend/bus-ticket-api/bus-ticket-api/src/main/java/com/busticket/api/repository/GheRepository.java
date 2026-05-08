package com.busticket.api.repository;

import com.busticket.api.entity.Ghe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GheRepository extends JpaRepository<Ghe, String> {

  List<Ghe> findByXe_MaXeOrderBySoGheAsc(String maXe);
}