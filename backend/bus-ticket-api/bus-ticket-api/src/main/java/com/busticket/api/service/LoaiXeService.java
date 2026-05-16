package com.busticket.api.service;

import com.busticket.api.dto.admin.CreateAdminLoaiXeRequest;
import com.busticket.api.dto.admin.UpdateAdminLoaiXeRequest;
import com.busticket.api.dto.loaixe.LoaiXeResponse;
import com.busticket.api.entity.LoaiXe;
import com.busticket.api.repository.LoaiXeRepository;
import com.busticket.api.repository.XeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoaiXeService {

  private final LoaiXeRepository loaiXeRepository;
  private final XeRepository xeRepository;

  public List<LoaiXeResponse> getAllLoaiXe() {
    return loaiXeRepository.findAll()
            .stream()
            .map(item -> new LoaiXeResponse(
                    item.getMaLoaiXe(),
          item.getTenLoaiXe(),
          item.getMoTa()
            ))
            .toList();
  }

  @Transactional
  public LoaiXeResponse createLoaiXe(CreateAdminLoaiXeRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu loai xe khong duoc de trong.");
    }

    String tenLoaiXe = requireText(request.getTenLoaiXe(), "Ten loai xe khong duoc de trong.");
    String maLoaiXe = normalizeOptionalText(request.getMaLoaiXe());

    if (maLoaiXe == null) {
      maLoaiXe = generateMaLoaiXe();
    }

    while (loaiXeRepository.existsById(maLoaiXe)) {
      maLoaiXe = generateMaLoaiXe();
    }

    loaiXeRepository.findByTenLoaiXe(tenLoaiXe)
            .ifPresent(item -> {
              throw new RuntimeException("Ten loai xe da ton tai.");
            });

    LoaiXe loaiXe = new LoaiXe();
    loaiXe.setMaLoaiXe(maLoaiXe);
    loaiXe.setTenLoaiXe(tenLoaiXe);
    loaiXe.setMoTa(normalizeOptionalText(request.getMoTa()));

    LoaiXe savedLoaiXe = loaiXeRepository.save(loaiXe);
        return new LoaiXeResponse(
          savedLoaiXe.getMaLoaiXe(),
          savedLoaiXe.getTenLoaiXe(),
          savedLoaiXe.getMoTa()
        );
  }

  @Transactional
  public LoaiXeResponse updateLoaiXe(String maLoaiXe, UpdateAdminLoaiXeRequest request) {
    if (request == null) {
      throw new RuntimeException("Du lieu loai xe khong duoc de trong.");
    }

    String ma = requireText(maLoaiXe, "Ma loai xe khong duoc de trong.");
    LoaiXe loaiXe = loaiXeRepository.findById(ma)
            .orElseThrow(() -> new RuntimeException("Khong tim thay loai xe."));

    if (request.getTenLoaiXe() != null) {
      String tenLoaiXe = requireText(request.getTenLoaiXe(), "Ten loai xe khong duoc de trong.");
      loaiXeRepository.findByTenLoaiXe(tenLoaiXe)
              .ifPresent(existing -> {
                if (!existing.getMaLoaiXe().equals(loaiXe.getMaLoaiXe())) {
                  throw new RuntimeException("Ten loai xe da ton tai.");
                }
              });
      loaiXe.setTenLoaiXe(tenLoaiXe);
    }

    if (request.getMoTa() != null) {
      loaiXe.setMoTa(normalizeOptionalText(request.getMoTa()));
    }

    LoaiXe savedLoaiXe = loaiXeRepository.save(loaiXe);
        return new LoaiXeResponse(
          savedLoaiXe.getMaLoaiXe(),
          savedLoaiXe.getTenLoaiXe(),
          savedLoaiXe.getMoTa()
        );
  }

  @Transactional
  public void deleteLoaiXe(String maLoaiXe) {
    String ma = requireText(maLoaiXe, "Ma loai xe khong duoc de trong.");

    LoaiXe loaiXe = loaiXeRepository.findById(ma)
            .orElseThrow(() -> new RuntimeException("Khong tim thay loai xe."));

    if (xeRepository.existsByLoaiXe_MaLoaiXe(ma)) {
      throw new RuntimeException("Khong the xoa loai xe dang duoc su dung.");
    }

    loaiXeRepository.delete(loaiXe);
  }

  private String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new RuntimeException(message);
    }

    return value.trim();
  }

  private String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private String generateMaLoaiXe() {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    int suffix = ThreadLocalRandom.current().nextInt(1000);
    return "LX" + timestamp + String.format("%03d", suffix);
  }
}
