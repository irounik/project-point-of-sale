package com.increff.ironic.pos.pojo;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "inventory")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class InventoryPojo extends BaseEntity<Integer> {

    @Id
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private Integer quantity;

    @Override
    public Integer getId() {
        return productId;
    }

    @Override
    public void setId(Integer id) {
        setProductId(id);
    }
}

