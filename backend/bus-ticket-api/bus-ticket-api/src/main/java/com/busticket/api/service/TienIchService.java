package com.busticket.api.service;

import com.busticket.api.dto.staffxe.TienIchResponse;
import com.busticket.api.repository.TienIchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TienIchService {

  private final TienIchRepository tienIchRepository;

  public List<TienIchResponse> getAllTienIch() {
    return tienIchRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(TienIch -> TienIch.getTenTienIch().toLowerCase()))
            .map(item -> new TienIchResponse(
                    item.getMaTienIch(),
                    item.getTenTienIch()
            ))
            .toList();
  }
}