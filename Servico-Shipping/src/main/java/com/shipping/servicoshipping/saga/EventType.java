package com.shipping.servicoshipping.saga;

public enum EventType {
    // já tinhas estes
    OrderCreated,
    OrderItemAdded,
    ShippingCreated,
    OrderTotalUpdated,
    CartClearRequested,
    CartCleared,
    OrderFinalizeRequested,
    OrderFinalized,
    OrderFinalizeFailed,
    StockReserveRequested,
    StockReleaseRequested;
}