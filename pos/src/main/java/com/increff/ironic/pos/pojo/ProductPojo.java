package com.increff.ironic.pos.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(
        name = "product",
        uniqueConstraints = {@UniqueConstraint(columnNames = "barcode")},
        indexes = {@Index(columnList = "barcode")}
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductPojo extends BaseEntity<Integer> {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String barcode;

    @Column(name = "brand_id", nullable = false)
    private Integer brandId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

}
