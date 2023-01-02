package com.increff.ironic.pos.model.data;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailsData {

    private Integer orderId;

    private LocalDateTime time;

    private List<OrderItemData> items;

}
