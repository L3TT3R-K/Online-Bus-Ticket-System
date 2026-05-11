package com.busticket.api.service;

import com.busticket.api.dto.payment.CreatePaymentResponse;
import com.busticket.api.entity.DatVe;
import com.busticket.api.entity.HoaDon;
import com.busticket.api.entity.KhuyenMai;
import com.busticket.api.entity.ThanhToan;
import com.busticket.api.entity.Ve;
import com.busticket.api.repository.DatVeRepository;
import com.busticket.api.repository.HoaDonRepository;
import com.busticket.api.repository.KhuyenMaiRepository;
import com.busticket.api.repository.ThanhToanRepository;
import com.busticket.api.repository.VeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;
import vn.payos.model.webhooks.WebhookData;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PayOS payOS;

  private final DatVeRepository datVeRepository;
  private final VeRepository veRepository;
  private final HoaDonRepository hoaDonRepository;
  private final ThanhToanRepository thanhToanRepository;
  private final KhuyenMaiRepository khuyenMaiRepository;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Transactional
  public CreatePaymentResponse createPayOSPayment(String maDatVe, String maKhuyenMai) {
    if (maDatVe == null || maDatVe.isBlank()) {
      throw new RuntimeException("Thiếu mã đặt vé.");
    }

    DatVe datVe = datVeRepository.findById(maDatVe)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt vé."));

    List<Ve> veList = veRepository.findByDatVe_MaDatVe(maDatVe);

    if (veList == null || veList.isEmpty()) {
      throw new RuntimeException("Đơn đặt vé chưa có vé.");
    }

    BigDecimal giaGoc = tinhGiaGoc(veList);

    if (giaGoc.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("Tổng tiền vé không hợp lệ.");
    }

    KhuyenMai khuyenMai = getKhuyenMaiHopLe(maKhuyenMai);
    BigDecimal tienGiam = tinhTienGiam(giaGoc, khuyenMai);
    BigDecimal tongTien = giaGoc.subtract(tienGiam);

    if (tongTien.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("Tổng tiền thanh toán không hợp lệ.");
    }

    HoaDon hoaDon = createOrUpdateHoaDon(datVe, giaGoc, tienGiam, tongTien, khuyenMai);

    Long orderCode = generateOrderCode();

    String description = buildPayOSDescription(hoaDon.getMaHoaDon());
    String returnUrl = frontendUrl + "/payment-success.html?orderCode=" + orderCode;
    String cancelUrl = frontendUrl + "/payment-cancel.html?orderCode=" + orderCode;

    try {
      CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
              .orderCode(orderCode)
              .amount(tongTien.setScale(0, RoundingMode.HALF_UP).longValue())
              .description(description)
              .returnUrl(returnUrl)
              .cancelUrl(cancelUrl)
              .build();

      var paymentLink = payOS.paymentRequests().create(paymentRequest);

      ThanhToan thanhToan = new ThanhToan();
      thanhToan.setMaThanhToan(generateMaThanhToan());
      thanhToan.setHoaDon(hoaDon);
      thanhToan.setOrderCode(orderCode);
      thanhToan.setPayOSPaymentLinkId(paymentLink.getPaymentLinkId());
      thanhToan.setCheckoutUrl(paymentLink.getCheckoutUrl());
      thanhToan.setLoaiGiaoDich("ThanhToan");
      thanhToan.setPhuongThucThanhToan("PAYOS");
      thanhToan.setSoTien(tongTien);
      thanhToan.setTrangThai("Đang xử lý");

      thanhToanRepository.save(thanhToan);

      datVe.setTrangThai("Chờ thanh toán");
      datVeRepository.save(datVe);

      for (Ve ve : veList) {
        ve.setTrangThai("Giữ chỗ");
      }

      veRepository.saveAll(veList);

      return new CreatePaymentResponse(
              datVe.getMaDatVe(),
              hoaDon.getMaHoaDon(),
              thanhToan.getMaThanhToan(),
              orderCode,
              paymentLink.getCheckoutUrl(),
              paymentLink.getPaymentLinkId(),
              thanhToan.getTrangThai()
      );
    } catch (Exception e) {
      throw new RuntimeException("Không tạo được link thanh toán payOS: " + e.getMessage());
    }
  }

  @Transactional
  public void handlePayOSWebhook(Map<String, Object> body) {
    try {
      WebhookData data = payOS.webhooks().verify(body);

      Long orderCode = data.getOrderCode();

      ThanhToan thanhToan = thanhToanRepository.findByOrderCode(orderCode)
              .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán với orderCode: " + orderCode));

      if ("Thành công".equals(thanhToan.getTrangThai())) {
        return;
      }

      boolean success = "00".equals(data.getCode());

      if (success) {
        markPaymentSuccess(thanhToan);
      } else {
        markPaymentFailed(thanhToan);
      }
    } catch (Exception e) {
      throw new RuntimeException("Webhook payOS không hợp lệ: " + e.getMessage());
    }
  }

  @Transactional
  public void cancelPayment(Long orderCode) {
    ThanhToan thanhToan = thanhToanRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán."));

    if ("Thành công".equals(thanhToan.getTrangThai())) {
      throw new RuntimeException("Thanh toán đã thành công, không thể hủy.");
    }

    thanhToan.setTrangThai("Đã hủy");
    thanhToanRepository.save(thanhToan);

    HoaDon hoaDon = thanhToan.getHoaDon();
    hoaDon.setTrangThai("Đã hủy");
    hoaDonRepository.save(hoaDon);

    DatVe datVe = hoaDon.getDatVe();
    datVe.setTrangThai("Đã hủy");
    datVeRepository.save(datVe);

    List<Ve> veList = veRepository.findByDatVe_MaDatVe(datVe.getMaDatVe());

    for (Ve ve : veList) {
      ve.setTrangThai("Đã hủy");
    }

    veRepository.saveAll(veList);
  }

  @Transactional
  public String syncPayOSPayment(Long orderCode) {
    ThanhToan thanhToan = thanhToanRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán."));

    if ("Thành công".equals(thanhToan.getTrangThai())) {
      return thanhToan.getTrangThai();
    }

    try {
      PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
      PaymentLinkStatus status = paymentLink.getStatus();

      if (PaymentLinkStatus.PAID.equals(status)) {
        markPaymentSuccess(thanhToan);
      } else if (PaymentLinkStatus.CANCELLED.equals(status)
              || PaymentLinkStatus.EXPIRED.equals(status)
              || PaymentLinkStatus.FAILED.equals(status)) {
        markPaymentFailed(thanhToan);
      }

      return thanhToan.getTrangThai();
    } catch (Exception e) {
      throw new RuntimeException("Không đồng bộ được trạng thái payOS: " + e.getMessage());
    }
  }

  private void markPaymentSuccess(ThanhToan thanhToan) {
    thanhToan.setTrangThai("Thành công");
    thanhToan.setNgayThanhToan(LocalDateTime.now());
    thanhToanRepository.save(thanhToan);

    HoaDon hoaDon = thanhToan.getHoaDon();
    hoaDon.setTrangThai("Đã thanh toán");
    hoaDonRepository.save(hoaDon);

    DatVe datVe = hoaDon.getDatVe();
    datVe.setTrangThai("Đã thanh toán");
    datVeRepository.save(datVe);

    List<Ve> veList = veRepository.findByDatVe_MaDatVe(datVe.getMaDatVe());

    for (Ve ve : veList) {
      ve.setTrangThai("Đã thanh toán");
    }

    veRepository.saveAll(veList);
  }

  private void markPaymentFailed(ThanhToan thanhToan) {
    thanhToan.setTrangThai("Không thành công");
    thanhToanRepository.save(thanhToan);
  }

  private HoaDon createOrUpdateHoaDon(
          DatVe datVe,
          BigDecimal giaGoc,
          BigDecimal tienGiam,
          BigDecimal tongTien,
          KhuyenMai khuyenMai
  ) {
    return hoaDonRepository.findByDatVe_MaDatVe(datVe.getMaDatVe())
            .map(existing -> {
              if ("Đã thanh toán".equals(existing.getTrangThai())) {
                throw new RuntimeException("Hóa đơn đã thanh toán.");
              }

              existing.setGiaGoc(giaGoc);
              existing.setTienGiam(tienGiam);
              existing.setTongTien(tongTien);
              existing.setKhuyenMai(khuyenMai);
              existing.setTrangThai("Chưa thanh toán");

              return hoaDonRepository.save(existing);
            })
            .orElseGet(() -> {
              HoaDon hoaDon = new HoaDon();
              hoaDon.setMaHoaDon(generateMaHoaDon());
              hoaDon.setDatVe(datVe);
              hoaDon.setGiaGoc(giaGoc);
              hoaDon.setTienGiam(tienGiam);
              hoaDon.setTongTien(tongTien);
              hoaDon.setKhuyenMai(khuyenMai);
              hoaDon.setTrangThai("Chưa thanh toán");

              return hoaDonRepository.save(hoaDon);
            });
  }

  private BigDecimal tinhGiaGoc(List<Ve> veList) {
    return veList.stream()
            .map(Ve::getGiaTien)
            .filter(giaTien -> giaTien != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private KhuyenMai getKhuyenMaiHopLe(String maKhuyenMai) {
    if (maKhuyenMai == null || maKhuyenMai.isBlank()) {
      return null;
    }

    KhuyenMai khuyenMai = khuyenMaiRepository
            .findByMaKhuyenMaiAndTrangThai(maKhuyenMai.trim(), "Hoạt động")
            .orElseThrow(() -> new RuntimeException("Khuyến mãi không hợp lệ hoặc đã tạm ngưng."));

    LocalDateTime now = LocalDateTime.now();

    if (khuyenMai.getNgayBatDau() != null && now.isBefore(khuyenMai.getNgayBatDau())) {
      throw new RuntimeException("Khuyến mãi chưa bắt đầu.");
    }

    if (khuyenMai.getNgayKetThuc() != null && now.isAfter(khuyenMai.getNgayKetThuc())) {
      throw new RuntimeException("Khuyến mãi đã hết hạn.");
    }

    return khuyenMai;
  }

  private BigDecimal tinhTienGiam(BigDecimal giaGoc, KhuyenMai khuyenMai) {
    if (khuyenMai == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal tienGiam = BigDecimal.ZERO;

    if (khuyenMai.getPhanTramGiam() != null
            && khuyenMai.getPhanTramGiam().compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal giamTheoPhanTram = giaGoc
              .multiply(khuyenMai.getPhanTramGiam())
              .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

      tienGiam = tienGiam.add(giamTheoPhanTram);
    }

    if (khuyenMai.getSoTienGiam() != null
            && khuyenMai.getSoTienGiam().compareTo(BigDecimal.ZERO) > 0) {
      tienGiam = tienGiam.add(khuyenMai.getSoTienGiam());
    }

    if (tienGiam.compareTo(giaGoc) > 0) {
      return giaGoc;
    }

    return tienGiam;
  }

  private Long generateOrderCode() {
    long orderCode;

    do {
      orderCode = System.currentTimeMillis();
    } while (thanhToanRepository.existsByOrderCode(orderCode));

    return orderCode;
  }

  private String generateMaHoaDon() {
    return "HD" + System.currentTimeMillis();
  }

  private String generateMaThanhToan() {
    return "TT" + System.currentTimeMillis();
  }

  private String buildPayOSDescription(String maHoaDon) {
    String text = "HD" + maHoaDon;

    if (text.length() > 25) {
      return text.substring(0, 25);
    }

    return text;
  }
}
