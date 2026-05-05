package com.busticket.api.repository;

import com.busticket.api.entity.TienIch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TienIchRepository extends JpaRepository<TienIch, String> {

  Optional<TienIch> findByTenTienIch(String tenTienIch);
}