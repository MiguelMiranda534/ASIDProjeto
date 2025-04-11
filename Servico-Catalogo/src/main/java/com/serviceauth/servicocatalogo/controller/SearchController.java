package com.serviceauth.servicocatalogo.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.serviceauth.servicocatalogo.entity.Book;
import com.serviceauth.servicocatalogo.service.BookService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/catalogo")
public class SearchController {

    @Autowired
    private BookService bookService;

    // Agora: GET /carrinho/pesquisa?query=...
    @GetMapping("/pesquisa")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam("query") String query) {
        List<Book> searchResults = bookService.searchBooks(query);
        if (searchResults != null && !searchResults.isEmpty()) {
            return new ResponseEntity<>(searchResults, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
