package com.serviceauth.servicocatalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Subcategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subcategoryid")
    private Long id;

    @Column(unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;

    // ISTO ESTAVA EM COMENTÁRIO
    //@OneToMany(mappedBy = "subcategory_id")
    //private Book book;
}
