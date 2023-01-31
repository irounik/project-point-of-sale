package com.increff.ironic.pos.model.data;

import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.pojo.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandOrderItems {
    private Brand brand;
    private List<OrderItem> orderItems;
}

