package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.OrderDto;
import com.increff.ironic.pos.model.form.OrderItemForm;
import com.increff.ironic.pos.service.ApiException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderApiController {

    @Autowired
    private OrderDto orderApiDto;

    @ApiOperation(value = "Gets list of the product with quantities")
    @RequestMapping(path = "/api/order", method = RequestMethod.POST)
    public void createOrder(@RequestBody List<OrderItemForm> orderItems) throws ApiException {
        orderApiDto.createOrder(orderItems);
    }

}
