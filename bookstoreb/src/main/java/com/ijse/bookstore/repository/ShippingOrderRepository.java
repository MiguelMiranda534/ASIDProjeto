package com.ijse.bookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ijse.bookstore.entity.ShippingOrder;

@Repository
public interface ShippingOrderRepository extends JpaRepository<ShippingOrder,Long>{
 
}
