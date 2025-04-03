package com.ijse.bookstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ijse.bookstore.entity.Book;
import com.ijse.bookstore.service.BookService;



@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class BookController {
    
    @Autowired
    private BookService bookSerivce; 

    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks(){
        List<Book> books = bookSerivce.getAllBook();
        
        return new ResponseEntity<>(books,HttpStatus.OK);
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id){

        Book existBook = bookSerivce.getBookById(id);

        if(existBook !=null){
            return new ResponseEntity<>(existBook,HttpStatus.OK);

        } else{

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/books/category/{id}")
    public ResponseEntity<List<Book>> getBooksByCategoryID(@PathVariable Long id) {

        List<Book> existBook = bookSerivce.getBooksByCategoryID(id);

         if(existBook !=null){

            return new ResponseEntity<>(existBook,HttpStatus.OK);

        } else{

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/updatequantity/{id}")
    public ResponseEntity<Book> patchQuantity(@PathVariable Long id , @RequestBody Book book){

        Book updatedBookQuantity = bookSerivce.patchBookQuantity(id,book);

        return new ResponseEntity<>(updatedBookQuantity,HttpStatus.OK);
    }
}
