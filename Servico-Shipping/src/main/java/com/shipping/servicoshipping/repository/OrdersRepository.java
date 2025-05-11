package com.shipping.servicoshipping.repository;

import com.shipping.servicoshipping.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Orders findByShippingOrderID(Long shippingOrderId);
    Orders findByUserId(Long userId);
}
