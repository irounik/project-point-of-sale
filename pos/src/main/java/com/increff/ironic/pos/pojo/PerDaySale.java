package com.increff.ironic.pos.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "per_day_sale")
@AllArgsConstructor
@NoArgsConstructor
public class PerDaySale extends BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime date;
    private Integer orderCount;
    private Integer uniqueItemCount; // barcode
    private Integer totalQuantityCount; // qty
    private Double totalRevenue;

}