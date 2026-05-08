package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "GHE")
public class Ghe {

  @Id
  @Column(name = "MAGHE", length = 20)
  private String maGhe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MAXE", nullable = false)
  private Xe xe;

  @Column(name = "SOGHE", length = 10, nullable = false)
  private String soGhe;
}