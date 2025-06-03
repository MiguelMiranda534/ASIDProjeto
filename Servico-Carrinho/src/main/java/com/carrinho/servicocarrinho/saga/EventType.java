// Servico-Carrinho/src/main/java/com/carrinho/servicocarrinho/saga/EventType.java
package com.carrinho.servicocarrinho.saga;

/**
 * Enum com todos os tipos de evento que a saga produz/consome.
 * Deve incluir todos os estados relevantes (p.ex. STOCK_RESERVED, etc).
 */
public enum EventType {
    CartLockRequested, CartLocked, CartLockFailed,
    OrderCreated, OrderItemAdded, ShippingCreated, OrderTotalUpdated,
    CartClearRequested, CartCleared
}