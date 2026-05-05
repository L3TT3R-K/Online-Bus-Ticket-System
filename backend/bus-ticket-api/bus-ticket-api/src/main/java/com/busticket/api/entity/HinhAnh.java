package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "HINHANH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HinhAnh {

  @Id
  @Column(name = "MAANH", length = 20)
  private String maAnh;

  @Column(name = "MAXE", nullable = false, length = 20)
  private String maXe;

  @Column(name = "URL", nullable = false, length = 500)
  private String url;

  @Column(name = "THUTU", nullable = false)
  private Integer thuTu;
}