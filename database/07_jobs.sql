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

-- ============================================================


