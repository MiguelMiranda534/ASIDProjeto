package com.example.servicosaga.controller;

import com.example.servicosaga.saga.CheckoutOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutOrchestrator orchestrator;

    public CheckoutController(CheckoutOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<String> start(@PathVariable Long userId, @RequestBody Map<String, Object> shippingDetails) {
        if (!shippingDetails.containsKey("firstName") ||
                !shippingDetails.containsKey("lastName") ||
                !shippingDetails.containsKey("address") ||
                !shippingDetails.containsKey("city") ||
                !shippingDetails.containsKey("email") ||
                !shippingDetails.containsKey("postal_code")) {
            return ResponseEntity.badRequest().body("Faltam detalhes de envio obrigatórios");
        }
        try {
            orchestrator.startSaga(userId, shippingDetails);
            return ResponseEntity.ok("Saga iniciada para o utilizador " + userId);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Erro ao iniciar saga: " + ex.getMessage());
        }
    }
}