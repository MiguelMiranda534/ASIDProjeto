package com.shipping.servicoshipping.controller;

import com.shipping.servicoshipping.entity.OrderDetails;
import com.shipping.servicoshipping.service.OrderDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class OrderDetailsController {
    
    @Autowired
    private OrderDetailsService orderDetailsService;

    @PostMapping("/orderdetails")
    public ResponseEntity<OrderDetails> createOrderDetails(@RequestBody OrderDetails orderDetails){

        OrderDetails orderedDetails = orderDetailsService.createOrderDetails(orderDetails);

        return new ResponseEntity<>(orderedDetails,HttpStatus.CREATED);

    }
}
