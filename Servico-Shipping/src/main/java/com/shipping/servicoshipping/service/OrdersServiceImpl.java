package com.shipping.servicoshipping.service;

import com.shipping.servicoshipping.entity.Orders;
import com.shipping.servicoshipping.repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Service
public class OrdersServiceImpl implements OrdersService {

    private static final Logger log = LoggerFactory.getLogger(OrdersServiceImpl.class);

    @Autowired
    private OrdersRepository ordersRepository;

    @Override
    public Orders createOrder(Long userId, Long shippingOrderId) {
        Orders o = new Orders();
        o.setUserId(userId);
        o.setOrderDate(new Date());
        o.setTotalPrice(0.0);
        o.setShippingOrderID(shippingOrderId);
        o.setStatus("PENDING");
        return ordersRepository.save(o);
    }

    @Override
    public Orders getOrderByShippingOrderId(Long shippingOrderId) {
        return ordersRepository.findByShippingOrderID(shippingOrderId);
    }

    @Override
    public void updateTotalPrice(Long orderId, double additionalAmount) {
        Orders o = ordersRepository.findById(orderId).orElse(null);
        if (o != null) {
            o.setTotalPrice(o.getTotalPrice() + additionalAmount);
            ordersRepository.save(o);
        }
    }

    /**
     * Finaliza a encomenda. A operação é idempotente:
     *  - devolve true se a ordem passar para CLOSED
     *  - devolve true se já estiver CLOSED
     *  - devolve false para estados inesperados ou inexistentes
     */
    @Override
    public boolean finalizeOrder(Long orderId) {
        log.info("🔍 [OrdersService] finalizeOrder chamado para orderId=" + orderId);
        Orders o = ordersRepository.findById(orderId).orElse(null);
        if (o == null) {
            System.out.println("⚠️ [OrdersService] Order " + orderId + " não encontrada");
            return false;
        }

        if ("CLOSED".equals(o.getStatus())) {
            System.out.println("ℹ️ [OrdersService] Order " + orderId + " já estava CLOSED");
            return true;
        }

        if (!"PENDING".equals(o.getStatus())) {
            System.out.println("⚠️ [OrdersService] Order " + orderId + " estado inesperado: " + o.getStatus());
            return false;
        }

        o.setStatus("CLOSED");
        ordersRepository.save(o);
        System.out.println("✅ [OrdersService] Order " + orderId + " passou de PENDING para CLOSED");
        return true;
    }

    @Override
    public Orders getOrderById(Long orderId) {
        return ordersRepository.findById(orderId).orElse(null);
    }
}