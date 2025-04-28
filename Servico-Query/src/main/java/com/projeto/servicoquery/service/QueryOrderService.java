package com.projeto.servicoquery.service;

import com.projeto.servicoquery.dto.OrderItemDTO;
import com.projeto.servicoquery.dto.OrderResponseDTO;
import com.projeto.servicoquery.dto.ShippingDTO;
import com.projeto.servicoquery.entity.QueryOrder;
import com.projeto.servicoquery.entity.QueryOrderItem;
import com.projeto.servicoquery.entity.QueryShipping;
import com.projeto.servicoquery.repository.QueryBookRepository;
import com.projeto.servicoquery.repository.QueryOrderItemRepository;
import com.projeto.servicoquery.repository.QueryOrderRepository;
import com.projeto.servicoquery.repository.QueryShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueryOrderService {

    @Autowired
    private QueryOrderRepository queryOrderRepository;

    @Autowired
    private QueryOrderItemRepository queryOrderItemRepository;

    @Autowired
    private QueryShippingRepository queryShippingRepository;

    @Autowired
    private QueryBookRepository queryBookRepository;

    public List<OrderResponseDTO> getOrdersBetweenDates(Long userId, Date startDate, Date endDate) {
        List<QueryOrder> orders = queryOrderRepository.findAll()
                .stream()
                .filter(o -> o.getUserId().equals(userId) &&
                        !o.getOrderDate().before(startDate) &&
                        !o.getOrderDate().after(endDate))
                .collect(Collectors.toList());

        return orders.stream().map(order -> {
            List<QueryOrderItem> items = queryOrderItemRepository.findAll()
                    .stream()
                    .filter(item -> item.getOrderId().equals(order.getId()))
                    .collect(Collectors.toList());

            QueryShipping shipping = queryShippingRepository.findAll()
                    .stream()
                    .filter(s -> s.getOrderId().equals(order.getId()))
                    .findFirst()
                    .orElse(null);

            OrderResponseDTO response = new OrderResponseDTO();
            response.setOrderId(order.getId());
            response.setOrderDate(order.getOrderDate());
            response.setTotalPrice(order.getTotalPrice());

            response.setItems(items.stream().map(i -> {
                OrderItemDTO dto = new OrderItemDTO();
                dto.setBookId(i.getBookId());
                dto.setQuantity(i.getQuantity());
                dto.setSubTotal(i.getSubTotal());

                // NOVO: buscar detalhes do livro
                queryBookRepository.findById(i.getBookId()).ifPresent(book -> {
                    dto.setBookTitle(book.getTitle());
                    dto.setAuthorName(book.getAuthor());
                    dto.setBookPrice(book.getPrice());
                });

                return dto;
            }).collect(Collectors.toList()));


            if (shipping != null) {
                ShippingDTO shippingDTO = new ShippingDTO();
                shippingDTO.setFirstName(shipping.getFirstName());
                shippingDTO.setLastName(shipping.getLastName());
                shippingDTO.setAddress(shipping.getAddress());
                shippingDTO.setCity(shipping.getCity());
                shippingDTO.setEmail(shipping.getEmail());
                shippingDTO.setPostalCode(shipping.getPostalCode());
                response.setShippingDetails(shippingDTO);
            }

            return response;
        }).collect(Collectors.toList());
    }
}
