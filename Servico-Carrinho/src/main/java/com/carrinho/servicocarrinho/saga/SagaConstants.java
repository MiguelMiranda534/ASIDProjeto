// Servico-Carrinho/src/main/java/com/carrinho/servicocarrinho/saga/SagaConstants.java
package com.carrinho.servicocarrinho.saga;

public final class SagaConstants {
    private SagaConstants() { /* impedir instâncias */ }

    // tópico Kafka partilhado por todas as sagas
    public static final String TOPIC = "checkout-events";
}