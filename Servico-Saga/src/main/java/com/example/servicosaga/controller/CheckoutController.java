// Servico-SAGA/src/main/java/com/projeto/servicosaga/controller/CheckoutController.java
package com.example.servicosaga.controller;

import com.example.servicosaga.saga.CheckoutOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CheckoutOrchestrator orchestrator;

    @PostMapping("/{userId}")
    public ResponseEntity<?> checkout(@PathVariable Long userId) {
        orchestrator.startSaga(userId);
        return ResponseEntity.accepted().body("Checkout iniciado para user " + userId);
    }
}