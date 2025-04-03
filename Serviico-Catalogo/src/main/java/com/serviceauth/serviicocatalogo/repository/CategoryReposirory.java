package com.serviceauth.serviicocatalogo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.serviceauth.serviicocatalogo.entity.Category;


@Repository
public interface CategoryReposirory extends JpaRepository<Category,Long>{
    
}
