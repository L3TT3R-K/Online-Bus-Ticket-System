package com.busticket.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TienIchXeId implements Serializable {

  @Column(name = "MAXE", length = 20)
  private String maXe;

  @Column(name = "MATIENICH", length = 20)
  private String maTienIch;
}