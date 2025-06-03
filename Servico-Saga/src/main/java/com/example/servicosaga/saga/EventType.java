package com.example.servicosaga.saga;

public enum EventType {
    CartLockRequested, CartLocked, CartLockFailed,
    StockReserveRequested, StockReserved, StockReserveFailed, StockReleaseRequested, StockReleased,
    CartClearRequested, CartCleared,
    OrderFinalizeRequested, OrderCreated, OrderFinalized, OrderFinalizeFailed, OrderItemAdded, OrderTotalUpdated,  ShippingCreated
}