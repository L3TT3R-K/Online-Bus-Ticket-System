package com.busticket.api.repository;

import com.busticket.api.entity.TienIchXe;
import com.busticket.api.entity.TienIchXeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TienIchXeRepository extends JpaRepository<TienIchXe, TienIchXeId> {

  void deleteByIdMaXe(String maXe);
}