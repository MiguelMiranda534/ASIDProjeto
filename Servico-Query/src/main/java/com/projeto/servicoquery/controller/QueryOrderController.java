package com.projeto.servicoquery.controller;

import com.projeto.servicoquery.dto.OrderResponseDTO;
import com.projeto.servicoquery.service.QueryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/query/orders")
public class QueryOrderController {

    @Autowired
    private QueryOrderService queryOrderService;

    @GetMapping("/{userId}")
    public List<OrderResponseDTO> getOrders(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return queryOrderService.getOrdersBetweenDates(userId, startDate, endDate);
    }
}
