package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TIENICH")
@Getter
@Setter
public class TienIch {

  @Id
  @Column(name = "MATIENICH", length = 20)
  private String maTienIch;

  @Column(name = "TENTIENICH", nullable = false, length = 100)
  private String tenTienIch;
}