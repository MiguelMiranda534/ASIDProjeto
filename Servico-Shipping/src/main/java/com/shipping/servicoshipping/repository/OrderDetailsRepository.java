package com.shipping.servicoshipping.repository;

import com.shipping.servicoshipping.entity.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetails,Long>{
    
}
