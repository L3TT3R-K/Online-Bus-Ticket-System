package com.busticket.api.controller;

import com.busticket.api.dto.staffxe.BenXeResponse;
import com.busticket.api.service.BenXeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ben-xe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BenXeController {

  private final BenXeService benXeService;

  @GetMapping
  public List<BenXeResponse> getAllBenXe() {
    return benXeService.getAllBenXe();
  }
}