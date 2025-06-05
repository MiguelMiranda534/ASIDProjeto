package com.shipping.servicoshipping.controller;

import com.shipping.servicoshipping.entity.OrderDetails;
import com.shipping.servicoshipping.service.OrderDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/order")
//@CrossOrigin(origins = "http://localhost:3000")
public class OrderDetailsController {
    
    @Autowired
    private OrderDetailsService orderDetailsService;

    @PostMapping("/details")
    public ResponseEntity<OrderDetails> createOrderDetails(@RequestBody OrderDetails orderDetails){

        OrderDetails orderedDetails = orderDetailsService.createOrderDetails(orderDetails);

        return new ResponseEntity<>(orderedDetails,HttpStatus.CREATED);

    }
    @GetMapping("/details/{id}")
    public ResponseEntity<?> getOrderDetailsById(@PathVariable Long id) {
        OrderDetails details = orderDetailsService.getById(id);
        if (details == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", "OrderDetails com id " + id + " não encontrado"
            ));
        }
        return new ResponseEntity<>(details, HttpStatus.OK);
    }

    @GetMapping("/details/shipping/{id}")
    public ResponseEntity<List<OrderDetails>> getOrderDetailsByShippingOrderId(@PathVariable Long id) {
        List<OrderDetails> detailsList = orderDetailsService.getByShippingOrderId(id);

        if (detailsList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
        }

        return new ResponseEntity<>(detailsList, HttpStatus.OK);
    }

}
