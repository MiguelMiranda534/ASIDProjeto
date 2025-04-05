package com.shipping.servicoshipping.controller;

import com.shipping.servicoshipping.entity.ShippingOrder;
import com.shipping.servicoshipping.service.ShippingOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ShippingOrderController {
    
    @Autowired
    private ShippingOrderService shippingOrderService;

    @PostMapping("/shipping")
    public ResponseEntity<ShippingOrder> createShippingOrder(@RequestBody ShippingOrder shippingOrder){

        ShippingOrder shippedOrder = shippingOrderService.createShippingOrder(shippingOrder);

        return new ResponseEntity<>(shippedOrder,HttpStatus.CREATED);
    }

    @GetMapping("/shipping")
    public ResponseEntity<List<ShippingOrder>> getAllShippingOrders(){

        List<ShippingOrder> shippedOrder = shippingOrderService.getAllShippingOrders();

        return new ResponseEntity<>(shippedOrder,HttpStatus.OK);
        
    }
}
