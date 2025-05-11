package com.example.servicosaga.saga;

public enum EventType {
    StockReserveRequested, StockReserved, StockReserveFailed, StockReleaseRequested, StockReleased,
    CartClearRequested, CartCleared,
    OrderFinalizeRequested, OrderFinalized, OrderFinalizeFailed,
    CheckoutCompleted, CheckoutFailed
}