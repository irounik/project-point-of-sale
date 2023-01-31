package com.increff.ironic.pos.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "order_items")
@Table(indexes = {@Index(columnList = "order_id")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem extends BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(nullable = false, name = "order_id")
    private Integer orderId;

    @Column(nullable = false, name = "product_id")
    private Integer productId;

    @Column(nullable = false, name = "selling_price")
    private Double sellingPrice;

    @Column(nullable = false)
    private Integer quantity;

    public OrderItem(Integer productId, Integer quantity, Double sellingPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.sellingPrice = sellingPrice;
    }

}