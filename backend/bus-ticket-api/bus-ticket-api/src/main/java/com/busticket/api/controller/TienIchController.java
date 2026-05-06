package com.busticket.api.controller;

import com.busticket.api.dto.staffxe.TienIchResponse;
import com.busticket.api.service.TienIchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tien-ich")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TienIchController {

  private final TienIchService tienIchService;

  @GetMapping
  public List<TienIchResponse> getAllTienIch() {
    return tienIchService.getAllTienIch();
  }
}