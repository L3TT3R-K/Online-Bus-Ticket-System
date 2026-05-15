-- PHẦN 7: JOB ĐỊNH KỲ
-- ============================================================

BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_GIAI_PHONG_GHE',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'DECLARE v NUMBER; BEGIN sp_giai_phong_ghe_het_han(v); END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=2',
        enabled         => TRUE,
        comments        => 'Giải phóng ghế Giữ chỗ đã hết ThoiGianGiuDen'
    );
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END;
/

-- Job cập nhật trạng thái chuyến xe tự động
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_CAP_NHAT_TRANGTHAI_CHUYEN',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN sp_cap_nhat_trangthai_chuyen; END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE,
        comments        => 'Cập nhật trạng thái chuyến xe tự động mỗi 1 phút'
    );
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END;
/
-- Job xóa vé / đơn vé đã hủy hoặc hết hạn quá 30 ngày

BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_XOA_VE_DA_HUY_CU',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'DECLARE v NUMBER; BEGIN sp_xoa_ve_da_huy_cu(v); END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY;BYHOUR=2;BYMINUTE=0;BYSECOND=0',
        enabled         => TRUE,
        comments        => 'Xóa vé / đơn vé đã hủy hoặc hết hạn quá 5 ngày'
    );
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END;
/

-- Job xóa tài khoản khách hàng chưa xác minh quá 3 ngày    

BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_XOA_TK_CHUA_XAC_MINH',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'DECLARE v NUMBER; BEGIN sp_xoa_tai_khoan_chua_xac_minh(v); END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY;BYHOUR=3;BYMINUTE=0;BYSECOND=0',
        enabled         => TRUE,
        comments        => 'Xóa tài khoản khách hàng chưa xác minh quá 3 ngày'
    );
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END;
/

-- ============================================================


