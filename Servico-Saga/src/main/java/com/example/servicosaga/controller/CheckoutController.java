package com.example.servicosaga.controller;

import com.example.servicosaga.saga.CheckoutOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutOrchestrator orchestrator;

    public CheckoutController(CheckoutOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @GetMapping("/saga/status/{sagaId}")
    public ResponseEntity<String> getSagaStatus(@PathVariable String sagaId) {
        String status = orchestrator.getSagaStatus(sagaId);
        return ResponseEntity.ok(status);
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
            String sagaId = UUID.randomUUID().toString();
            orchestrator.startSaga(sagaId, userId, shippingDetails);
            return ResponseEntity.ok("Saga iniciada com ID: " + sagaId);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Erro ao iniciar saga: " + ex.getMessage());
        }
    }
}