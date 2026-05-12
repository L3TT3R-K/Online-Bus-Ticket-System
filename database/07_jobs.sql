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

-- ============================================================


