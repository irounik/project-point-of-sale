package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.OrderDto;
import com.increff.ironic.pos.model.data.OrderData;
import com.increff.ironic.pos.model.data.OrderDetailsData;
import com.increff.ironic.pos.model.form.OrderItemForm;
import com.increff.ironic.pos.service.ApiException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/orders")
public class OrderApiController {

    @Autowired
    private OrderDto orderApiDto;

    @ApiOperation(value = "Gets list of the product with quantities")
    @RequestMapping(path = "/", method = RequestMethod.POST)
    public void createOrder(@RequestBody List<OrderItemForm> orderItems) throws ApiException {
        orderApiDto.createOrder(orderItems);
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public List<OrderData> getAllOrders() {
        return orderApiDto.getAll();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public OrderDetailsData getOrderDetails(@PathVariable Integer id) throws ApiException {
        return orderApiDto.getOrderDetails(id);
    }

    @RequestMapping(path = "/{orderId}", method = RequestMethod.PUT)
    public void updateOrder(
            @PathVariable Integer orderId,
            @RequestBody List<OrderItemForm> updatedItems
    ) throws ApiException {
        orderApiDto.updateOrder(orderId, updatedItems);
    }

}
