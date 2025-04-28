package com.projeto.servicoquery.repository;

import com.projeto.servicoquery.entity.QueryShipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryShippingRepository extends JpaRepository<QueryShipping, Long> {
}
