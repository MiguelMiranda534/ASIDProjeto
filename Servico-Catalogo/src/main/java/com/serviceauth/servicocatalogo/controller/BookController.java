package com.serviceauth.servicocatalogo.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.serviceauth.servicocatalogo.entity.Book;
import com.serviceauth.servicocatalogo.service.BookService;

//@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/catalogo")
public class BookController {

    @Autowired
    private BookService bookSerivce;

    // Agora o endpoint fica: GET /catalogo/livros
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks(){
        List<Book> books = bookSerivce.getAllBook();
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    // Agora: GET /catalogo/livros/{id}
    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id){
        Book existBook = bookSerivce.getBookById(id);
        if(existBook != null){
            return new ResponseEntity<>(existBook, HttpStatus.OK);
        } else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Agora: GET /catalogo/categoria/{id}
    @GetMapping("/category/{id}")
    public ResponseEntity<List<Book>> getBooksByCategoryID(@PathVariable Long id) {
        List<Book> existBook = bookSerivce.getBooksByCategoryID(id);
        if(existBook != null){
            return new ResponseEntity<>(existBook, HttpStatus.OK);
        } else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Agora: PATCH /catalogo/atualizarquantidade/{id}
    @PatchMapping("/updatequantity/{id}")
    public ResponseEntity<Book> patchQuantity(@PathVariable Long id , @RequestBody Book book){
        Book updatedBookQuantity = bookSerivce.patchBookQuantity(id, book);
        return new ResponseEntity<>(updatedBookQuantity, HttpStatus.OK);
    }
}
