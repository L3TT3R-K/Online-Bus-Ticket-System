package com.busticket.api.service;

import com.busticket.api.dto.nhaxe.NhaXeResponse;
import com.busticket.api.entity.NhaXe;
import com.busticket.api.repository.NhaXeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NhaXeService {

  private final NhaXeRepository nhaXeRepository;

  public List<NhaXeResponse> getAllNhaXe() {
    return nhaXeRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(NhaXe::getTenNhaXe))
            .map(this::mapToResponse)
            .toList();
  }

  private NhaXeResponse mapToResponse(NhaXe nhaXe) {
    return new NhaXeResponse(
            nhaXe.getMaNhaXe(),
            nhaXe.getTenNhaXe(),
            nhaXe.getSdt(),
            nhaXe.getEmail(),
            nhaXe.getDiaChi(),
            nhaXe.getMoTa(),
            nhaXe.getTrangThai()
    );
  }
}
