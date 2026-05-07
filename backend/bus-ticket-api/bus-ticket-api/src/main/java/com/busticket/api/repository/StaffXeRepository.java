package com.busticket.api.repository;

import com.busticket.api.dto.staffxe.StaffXeProjection;
import com.busticket.api.entity.Xe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffXeRepository extends JpaRepository<Xe, String> {

  boolean existsByBienSo(String bienSo);

  Optional<Xe> findByBienSo(String bienSo);

  Optional<Xe> findByMaXeAndNhaXe_MaNhaXe(String maXe, String maNhaXe);

  @Query(value = """
            SELECT
                x.MAXE AS "maXe",
                x.BIENSO AS "bienSo",
                x.MALOAIXE AS "maLoaiXe",
                lx.TENLOAIXE AS "tenLoaiXe",
                x.SOLUONGGHE AS "soLuongGhe",
                x.TRANGTHAI AS "trangThai",

                (
                    SELECT LISTAGG(ha.URL, '||') WITHIN GROUP (ORDER BY ha.THUTU)
                    FROM HINHANH ha
                    WHERE ha.MAXE = x.MAXE
                ) AS "imageUrls",

                (
                    SELECT LISTAGG(ti.TENTIENICH, '||') WITHIN GROUP (ORDER BY ti.TENTIENICH)
                    FROM TIENICHXE tix
                    JOIN TIENICH ti ON tix.MATIENICH = ti.MATIENICH
                    WHERE tix.MAXE = x.MAXE
                ) AS "amenities"

            FROM XE x
            JOIN LOAIXE lx ON x.MALOAIXE = lx.MALOAIXE
            WHERE x.MANHAXE = :maNhaXe
            ORDER BY x.MAXE DESC
            """, nativeQuery = true)
  List<StaffXeProjection> findXeByNhaXe(@Param("maNhaXe") String maNhaXe);


}
