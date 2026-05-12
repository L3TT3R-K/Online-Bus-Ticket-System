package com.busticket.api.service;

import com.busticket.api.repository.ChuyenXeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * Scheduler để tự động cập nhật trạng thái chuyến xe
 * Chạy mỗi 1 phút để đồng bộ dữ liệu từ Oracle
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChuyenXeScheduler {

    private final ChuyenXeRepository chuyenXeRepository;

    /**
     * Cập nhật trạng thái chuyến xe tự động
     * - Chuyến thành "Đang chạy" khi hiện tại >= thời gian khởi hành
     * - Chuyến thành "Hoàn thành" khi hiện tại >= thời gian đến
     * Chạy mỗi 60 giây (1 phút)
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 5000)
    public void capNhatTrangThaiChuyen() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            log.info("=== Bắt đầu cập nhật trạng thái chuyến xe lúc: {} ===", now);
            
            // Cập nhật chuyến thành "Đang chạy"
            int soDangChay = chuyenXeRepository.updateTrangThaiDangChay(now);
            if (soDangChay > 0) {
                log.info("✓ Cập nhật {} chuyến thành 'Đang chạy'", soDangChay);
            }
            
            // Cập nhật chuyến thành "Hoàn thành"
            int soHoanThanh = chuyenXeRepository.updateTrangThaiHoanThanh(now);
            if (soHoanThanh > 0) {
                log.info("✓ Cập nhật {} chuyến thành 'Hoàn thành'", soHoanThanh);
            }
            
            if (soDangChay == 0 && soHoanThanh == 0) {
                log.debug("Không có chuyến cần cập nhật trạng thái");
            }
            
            log.info("=== Kết thúc cập nhật trạng thái chuyến xe ===");
            
        } catch (Exception e) {
            log.error(" Lỗi khi cập nhật trạng thái chuyến xe: ", e);
        }
    }
}
