package com.increff.ironic.pos.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
// TODO: 29/01/23 why ToString?
@ToString
@Entity
public class Inventory extends BaseEntity<Integer> {

    @Id
    @Column(name = "product_id")
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

