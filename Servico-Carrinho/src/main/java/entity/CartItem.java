package entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column
    private int quantity;

    @Column
    private Double unitPrice;

    @Column
    private Double subTotal;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book bookid;

    @ManyToOne
    @JoinColumn(name = "user_id") 
    private User user;
    
}
