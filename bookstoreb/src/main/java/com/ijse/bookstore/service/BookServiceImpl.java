package com.ijse.bookstore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ijse.bookstore.entity.Book;
import com.ijse.bookstore.repository.BookRepository;


@Service
public class BookServiceImpl implements BookService {
    
    @Autowired
    private BookRepository bookRepository;

    

    @Override
    public List<Book> getAllBook(){

        return bookRepository.findAll();
    }


    @Override
    public Book getBookById(Long id){


        return bookRepository.findById(id).orElse(null);
    }

    @Override
    public List<Book> getBooksByCategoryID(Long id){

        return bookRepository.findByCategoryId(id);
           
            
    }

    @Override
    public List<Book> searchBooks(String query) {
        return bookRepository.searchBooks(query);
    }
       
    @Override
    public Book patchBookQuantity(Long id, Book book){

        Book existBook = bookRepository.findById(id).orElse(null);

        if (existBook != null) {
            
            existBook.setQuantity(book.getQuantity());
            bookRepository.save(existBook);
    
            return existBook;
        } else { 
            return null;
        }
    }
}
