package com.serviceauth.servicocatalogo.saga;

/**
 * Tipos de evento do Stock-Saga.
 */
public enum EventType {
    StockReserveRequested,
    StockReleaseRequested,
    StockReserved,
    StockReserveFailed,
    StockReleased
}