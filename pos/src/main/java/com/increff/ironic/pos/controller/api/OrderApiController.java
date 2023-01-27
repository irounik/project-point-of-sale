package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.OrderApiDto;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.OrderData;
import com.increff.ironic.pos.model.data.OrderDetailsData;
import com.increff.ironic.pos.model.form.OrderItemForm;
import com.increff.ironic.pos.pojo.Order;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
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

    @Autowired
    public OrderApiController(OrderApiDto orderApiDto) {
        this.orderApiDto = orderApiDto;
    }

    // TODO: 24/01/23 adding,editing methods should return the data similar to add method in brand controller
    @ApiOperation(value = "Gets list of the product with quantities")
    @RequestMapping(path = "", method = RequestMethod.POST)
    public void createOrder(@RequestBody List<OrderItemForm> orderItems) throws ApiException {
        orderApiDto.createOrder(orderItems);
    }

    @RequestMapping(path = "", method = RequestMethod.GET)
    public List<OrderData> getAllOrders() {
        return orderApiDto.getAll();
    }

    // TODO: 24/01/23 use the same variable name everywhere either orderId or id
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public OrderDetailsData getOrderDetails(@PathVariable Integer id) throws ApiException {
        return orderApiDto.getOrderDetails(id);
    }

    // TODO: 24/01/23 adding,editing methods should return the data similar to add method in brand controller
    @RequestMapping(path = "/{orderId}", method = RequestMethod.PUT)
    public void updateOrder(
            @PathVariable Integer orderId,
            @RequestBody List<OrderItemForm> updatedItems
    ) throws ApiException {
        orderApiDto.updateOrder(orderId, updatedItems);
    }

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
            throw new ApiException("Error occured while downloading invoice!");
        }
    }

}
