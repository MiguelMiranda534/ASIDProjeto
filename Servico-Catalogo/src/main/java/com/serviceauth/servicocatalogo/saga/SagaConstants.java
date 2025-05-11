package com.serviceauth.servicocatalogo.saga;


public final class SagaConstants {
    private SagaConstants() { }

    /** Tópico Kafka onde todos os eventos do Saga são publicados. */
    public static final String TOPIC = "checkout-events";
}