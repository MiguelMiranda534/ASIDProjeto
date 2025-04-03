package com.serviceauth.serviicocatalogo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.serviceauth.serviicocatalogo.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book,Long> {

    @Query("SELECT b FROM Book b " +
       "WHERE (:query is null OR " +
       "b.title LIKE %:query% OR " +
       "b.author.authorName LIKE %:query% OR " +
       "b.category.name LIKE %:query% OR " +
       "b.subcategory.name LIKE %:query%)")
    List<Book> searchBooks(@Param("query") String query);


    List<Book> findByCategoryId(Long id);

    

}
