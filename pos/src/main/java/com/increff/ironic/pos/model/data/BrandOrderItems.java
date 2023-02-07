package com.increff.ironic.pos.model.data;

import com.increff.ironic.pos.pojo.BrandPojo;
import com.increff.ironic.pos.pojo.OrderItemPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandOrderItems {
    private BrandPojo brandPojo;
    private List<OrderItemPojo> orderItemEntities;
}

