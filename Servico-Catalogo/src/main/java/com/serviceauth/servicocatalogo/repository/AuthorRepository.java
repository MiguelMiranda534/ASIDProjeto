package com.serviceauth.servicocatalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serviceauth.servicocatalogo.entity.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author,Long> {
    
}
