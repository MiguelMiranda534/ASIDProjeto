package com.serviceauth.servicocatalogo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.serviceauth.servicocatalogo.entity.Category;


@Repository
public interface CategoryReposirory extends JpaRepository<Category,Long>{
    
}
