package com.busticket.api.service;

import com.busticket.api.dto.LoaiXeResponse;
import com.busticket.api.repository.LoaiXeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoaiXeService {

  private final LoaiXeRepository loaiXeRepository;

  public List<LoaiXeResponse> getAllLoaiXe() {
    return loaiXeRepository.findAll()
            .stream()
            .map(item -> new LoaiXeResponse(
                    item.getMaLoaiXe(),
                    item.getTenLoaiXe()
            ))
            .toList();
  }
}