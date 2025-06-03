package com.example.servicosaga.controller;

import com.example.servicosaga.saga.CheckoutOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutOrchestrator orchestrator;

    public CheckoutController(CheckoutOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /** Inicia a saga para o utilizador indicado. */
    @PostMapping("/{userId}")
    public ResponseEntity<String> start(@PathVariable Long userId) {
        try {
            orchestrator.startSaga(userId);
            return ResponseEntity.ok("Saga iniciada para o utilizador " + userId);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Erro ao iniciar saga: " + ex.getMessage());
        }
    }
}