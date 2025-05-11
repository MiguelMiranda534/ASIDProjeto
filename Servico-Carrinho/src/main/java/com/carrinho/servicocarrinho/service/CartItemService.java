package com.carrinho.servicocarrinho.service;

import com.carrinho.servicocarrinho.entity.CartItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartItemService {
    List<CartItem> getAllCartitem();
    CartItem createCartItem(CartItem cartItem);
    CartItem getCartItemById(Long id);
    CartItem patchCartQuantity(Long id , CartItem cartItem);
    CartItem patchCartSubTotal(Long id , CartItem cartItem);
    CartItem deleteCartItyItemById(Long id);
    void clearCart();
    void resetAutoIncrement();

    // novo: apagar só os itens desse user
    void clearCartForUser(String userId);

    List<CartItem> getCartItemsByUsername(String username);
}
