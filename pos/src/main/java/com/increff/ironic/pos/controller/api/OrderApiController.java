package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.OrderApiDto;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.OrderData;
import com.increff.ironic.pos.model.data.OrderDetailsData;
import com.increff.ironic.pos.model.form.OrderItemForm;
import com.increff.ironic.pos.pojo.Order;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/api/orders")
public class OrderApiController {

    private final OrderApiDto orderApiDto;

    private static final Logger logger = Logger.getLogger(OrderApiController.class);

    @Autowired
    public OrderApiController(OrderApiDto orderApiDto) {
        this.orderApiDto = orderApiDto;
    }

    @ApiOperation(value = "Creates an order")
    @RequestMapping(path = "", method = RequestMethod.POST)
    public Order createOrder(@RequestBody List<OrderItemForm> orderItems) throws ApiException {
        return orderApiDto.createOrder(orderItems);
    }

    @ApiOperation(value = "Get all orders")
    @RequestMapping(path = "", method = RequestMethod.GET)
    public List<OrderData> getAllOrders() {
        return orderApiDto.getAll();
    }

    @ApiOperation(value = "Get details for a specific order by ID")
    @RequestMapping(path = "/{orderId}", method = RequestMethod.GET)
    public OrderDetailsData getOrderDetails(@PathVariable Integer orderId) throws ApiException {
        return orderApiDto.getOrderDetails(orderId);
    }

    @ApiOperation(value = "Update an order by ID")
    @RequestMapping(path = "/{orderId}", method = RequestMethod.PUT)
    public Order updateOrder(
            @PathVariable Integer orderId,
            @RequestBody List<OrderItemForm> updatedItems
    ) throws ApiException {
        return orderApiDto.updateOrder(orderId, updatedItems);
    }

    @ApiOperation(value = "Download invoice for a specific order")
    @RequestMapping(path = "/invoice/{orderId}", method = RequestMethod.GET)
    public void downloadInvoice(@PathVariable Integer orderId, HttpServletResponse response) throws ApiException {
        Order order = orderApiDto.getOrder(orderId);
        String filePath = order.getInvoicePath();

        response.setContentType("application/pdf");
        response.addHeader("Content-disposition:", "attachment; filename=invoice-" + orderId);

        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            IOUtils.copy(fileInputStream, response.getOutputStream());
            fileInputStream.close();
            response.flushBuffer();
        } catch (IOException e) {
            String message = "Error occured while downloading invoice!";
            logger.error(message, e);
            throw new ApiException(message);
        }
    }

}
