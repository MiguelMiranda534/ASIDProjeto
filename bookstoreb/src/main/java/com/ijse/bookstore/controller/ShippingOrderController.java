package com.ijse.bookstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ijse.bookstore.entity.ShippingOrder;
import com.ijse.bookstore.service.ShippingOrderService;


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
