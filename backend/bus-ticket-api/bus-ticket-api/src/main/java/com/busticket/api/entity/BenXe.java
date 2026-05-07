package com.busticket.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "BENXE")
public class BenXe {

  @Id
  @Column(name = "MABEN", length = 20)
  private String maBen;

  @Column(name = "TENBEN", length = 100)
  private String tenBen;

  @Column(name = "DIACHI", length = 200)
  private String diaChi;
}