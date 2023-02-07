package com.increff.ironic.pos.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "per_day_sale")
@Table(indexes = {@Index(columnList = "date")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PerDaySalePojo extends BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount;

    @Column(name = "unique_item_count", nullable = false)
    private Integer uniqueItemCount;

    @Column(name = "total_quantity_count", nullable = false)
    private Integer totalQuantityCount;

    @Column(name = "total_revenue", nullable = false)
    private Double totalRevenue;

}
