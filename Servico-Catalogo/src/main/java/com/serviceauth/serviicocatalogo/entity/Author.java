package com.serviceauth.serviicocatalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Author {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "authorid")
    private Long id;

    @Column(unique = true , nullable = false)
    private String authorName;


    // ISTO ESTAVA EM COMENTÁRIO
    //@OneToMany(mappedBy = "authorid")
    //private Book book;
}
