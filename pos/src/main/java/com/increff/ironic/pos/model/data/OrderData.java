package com.increff.ironic.pos.model.data;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderData {

    private Integer id;

    private LocalDateTime time;

}
