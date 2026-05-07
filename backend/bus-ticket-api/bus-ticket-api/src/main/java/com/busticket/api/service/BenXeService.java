package com.busticket.api.service;

import com.busticket.api.dto.staffxe.BenXeResponse;
import com.busticket.api.repository.BenXeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BenXeService {

  private final BenXeRepository benXeRepository;

  public List<BenXeResponse> getAllBenXe() {
    return benXeRepository.findAllByOrderByTenBenAsc()
            .stream()
            .map(item -> new BenXeResponse(
                    item.getMaBen(),
                    item.getTenBen(),
                    item.getDiaChi()
            ))
            .toList();
  }
}