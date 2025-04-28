package com.projeto.servicoquery.repository;

import com.projeto.servicoquery.entity.QueryBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryBookRepository extends JpaRepository<QueryBook, Long> {
}
