package com.ijse.bookstore.controller;

import com.ijse.bookstore.entity.Cart;
import com.ijse.bookstore.entity.Orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ijse.bookstore.entity.OrderDetails;
import com.ijse.bookstore.service.OrderDetailsService;

import java.util.List;


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

    @GetMapping("/orderdetails")
    public ResponseEntity<List<Orders>> getAllOrders(){

        List<Orders> existorder = orderDetailsService.getAllOrders();

        return new ResponseEntity<>(existorder,HttpStatus.OK);
    }
}
