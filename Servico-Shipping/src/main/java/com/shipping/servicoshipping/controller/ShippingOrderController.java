package com.shipping.servicoshipping.controller;

import com.shipping.servicoshipping.entity.ShippingOrder;
import com.shipping.servicoshipping.service.ShippingOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/order")
@CrossOrigin(origins = "http://localhost:3000")
public class ShippingOrderController {
    
    @Autowired
    private ShippingOrderService shippingOrderService;

    @PostMapping("/shipping")
    public ResponseEntity<ShippingOrder> createShippingOrder(@RequestBody Map<String,Object> body){

        System.out.println("📥 [ShippingOrderController] recebido POST /order/shipping → body=" + body);

        // em vez de receber diretamente um ShippingOrder, recebemos um JSON genérico
        Long userId      = Long.valueOf(body.get("userId").toString());
        String firstName = body.get("firstName").toString();
        String lastName  = body.get("lastName").toString();
        String address   = body.get("address").toString();
        String city      = body.get("city").toString();
        String email     = body.get("email").toString();
        String postal    = body.get("postal_code").toString();
        String sagaId    = body.get("sagaId").toString(); // ← capturar aqui

        ShippingOrder shippingOrder = new ShippingOrder();
        shippingOrder.setUserId(userId);
        shippingOrder.setFirstName(firstName);
        shippingOrder.setLastName(lastName);
        shippingOrder.setAddress(address);
        shippingOrder.setCity(city);
        shippingOrder.setEmail(email);
        shippingOrder.setPostal_code(postal);

        ShippingOrder created = shippingOrderService.createShippingOrder(shippingOrder, sagaId);
        System.out.println("✅ [ShippingOrderController] retornando ShippingOrder criado: " + created);

        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/shipping")
    public ResponseEntity<List<ShippingOrder>> getAllShippingOrders(){

        List<ShippingOrder> shippedOrder = shippingOrderService.getAllShippingOrders();

        return new ResponseEntity<>(shippedOrder,HttpStatus.OK);
        
    }

    @GetMapping("/shipping/{id}")
    public ResponseEntity<ShippingOrder> getShippingOrderById(@PathVariable Long id) {
        ShippingOrder shipping = shippingOrderService.getById(id);
        System.out.println("🚚 Shipping encontrado: " + shipping);
        return new ResponseEntity<>(shipping, HttpStatus.OK);
    }

}
