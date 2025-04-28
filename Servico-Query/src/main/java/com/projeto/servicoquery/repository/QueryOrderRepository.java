package com.projeto.servicoquery.repository;

import com.projeto.servicoquery.entity.QueryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryOrderRepository extends JpaRepository<QueryOrder, Long> {
}
