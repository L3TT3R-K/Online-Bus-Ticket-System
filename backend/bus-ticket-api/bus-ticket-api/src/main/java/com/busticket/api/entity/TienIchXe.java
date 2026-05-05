package com.busticket.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TIENICHXE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TienIchXe {

  @EmbeddedId
  private TienIchXeId id;
}