package com.loopers.application.cart;

import com.loopers.domain.cart.Cart;
import com.loopers.domain.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class CartFacade {

    private final CartService cartService;

    public CartInfo addToCart(CartCommand command) {
        // service
        Cart cart = cartService.addToCart(command);
        // domain -> info
        return CartInfo.from(cart);
    }

    public CartInfo getMyCart(String userId) {
        // service
        Cart cart = cartService.getMyCart(userId);
        // domain -> info
        return CartInfo.from(cart);
    }

}
