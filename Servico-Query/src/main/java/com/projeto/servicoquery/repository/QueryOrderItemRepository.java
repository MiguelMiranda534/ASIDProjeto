package com.projeto.servicoquery.repository;

import com.projeto.servicoquery.entity.QueryOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryOrderItemRepository extends JpaRepository<QueryOrderItem, Long> {
}
