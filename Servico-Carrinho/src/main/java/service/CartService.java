package service;

import com.ijse.bookstore.entity.Cart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartService {

    Cart createCart(Cart cart);
    List<Cart> getAllCart();
    Cart getCartIdByUserId(Long userId);
}
