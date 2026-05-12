package com.busticket.api.service;

import com.busticket.api.dto.danhgia.CreateDanhGiaRequest;
import com.busticket.api.dto.danhgia.CreateDanhGiaResponse;
import com.busticket.api.entity.ChuyenXe;
import com.busticket.api.entity.DanhGia;
import com.busticket.api.entity.KhachHang;
import com.busticket.api.entity.NhaXe;
import com.busticket.api.repository.ChuyenXeRepository;
import com.busticket.api.repository.DanhGiaRepository;
import com.busticket.api.repository.KhachHangRepository;
import com.busticket.api.repository.VeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DanhGiaService {

  private static final List<String> REVIEWABLE_TICKET_STATUSES = List.of(
          "Đã đặt",
          "Đã thanh toán",
          "Đã dùng"
  );

  private final DanhGiaRepository danhGiaRepository;
  private final KhachHangRepository khachHangRepository;
  private final ChuyenXeRepository chuyenXeRepository;
  private final VeRepository veRepository;

  @Transactional
  public CreateDanhGiaResponse create(CreateDanhGiaRequest request) {
    if (request == null) {
      throw new RuntimeException("Dữ liệu đánh giá không được để trống.");
    }

    String maKhachHang = requireText(
            request.getMaKhachHang(),
            "Mã khách hàng không được để trống."
    );
    String maChuyen = requireText(
            request.getMaChuyen(),
            "Mã chuyến không được để trống."
    );
    String maNhaXe = requireText(
            request.getMaNhaXe(),
            "Mã nhà xe không được để trống."
    );
    Integer soSao = requireRating(request.getSoSao());
    String noiDung = normalizeOptionalText(request.getNoiDung());

    KhachHang khachHang = khachHangRepository.findById(maKhachHang)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng."));

    ChuyenXe chuyenXe = chuyenXeRepository.findById(maChuyen)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến xe."));

    NhaXe nhaXe = resolveNhaXe(chuyenXe);
    if (!maNhaXe.equals(nhaXe.getMaNhaXe())) {
      throw new RuntimeException("Mã nhà xe không khớp với chuyến được đánh giá.");
    }

    if (!"Hoàn thành".equals(chuyenXe.getTrangThai())) {
      throw new RuntimeException("Chỉ được đánh giá chuyến đã hoàn thành.");
    }

    boolean hasEligibleTicket =
            veRepository.existsByKhachHang_MaKHAndChuyenXe_MaChuyenAndTrangThaiIn(
                    maKhachHang,
                    maChuyen,
                    REVIEWABLE_TICKET_STATUSES
            );

    if (!hasEligibleTicket) {
      throw new RuntimeException("Bạn chưa có vé hợp lệ cho chuyến này.");
    }

    if (danhGiaRepository.existsByKhachHang_MaKHAndChuyenXe_MaChuyen(maKhachHang, maChuyen)) {
      throw new RuntimeException("Bạn đã đánh giá chuyến này.");
    }

    DanhGia danhGia = new DanhGia();
    danhGia.setMaDanhGia(generateMaDanhGia());
    danhGia.setKhachHang(khachHang);
    danhGia.setChuyenXe(chuyenXe);
    danhGia.setSoSao(soSao);
    danhGia.setNoiDung(noiDung);
    danhGia.setNgayDanhGia(LocalDateTime.now());

    DanhGia savedDanhGia = danhGiaRepository.save(danhGia);

    return new CreateDanhGiaResponse(
            savedDanhGia.getMaDanhGia(),
            khachHang.getMaKH(),
            chuyenXe.getMaChuyen(),
            nhaXe.getMaNhaXe(),
            savedDanhGia.getSoSao(),
            savedDanhGia.getNoiDung(),
            savedDanhGia.getNgayDanhGia()
    );
  }

  private String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new RuntimeException(message);
    }

    return value.trim();
  }

  private Integer requireRating(Integer soSao) {
    if (soSao == null) {
      throw new RuntimeException("Số sao không được để trống.");
    }

    if (soSao < 1 || soSao > 5) {
      throw new RuntimeException("Số sao phải nằm trong khoảng từ 1 đến 5.");
    }

    return soSao;
  }

  private String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private NhaXe resolveNhaXe(ChuyenXe chuyenXe) {
    if (chuyenXe.getXe() == null || chuyenXe.getXe().getNhaXe() == null) {
      throw new RuntimeException("Không xác định được nhà xe của chuyến.");
    }

    return chuyenXe.getXe().getNhaXe();
  }

  private String generateMaDanhGia() {
    String maDanhGia;

    do {
      maDanhGia = "DG" + System.currentTimeMillis();
    } while (danhGiaRepository.existsById(maDanhGia));

    return maDanhGia;
  }
}
