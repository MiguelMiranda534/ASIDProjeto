package com.shipping.servicoshipping.repository;

import com.shipping.servicoshipping.entity.ShippingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingOrderRepository extends JpaRepository<ShippingOrder,Long>{
 
}
