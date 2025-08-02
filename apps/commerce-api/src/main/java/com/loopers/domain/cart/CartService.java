package com.loopers.domain.cart;


import com.loopers.application.cart.CartCommand;
import com.loopers.application.cart.CartItemCommand;
import com.loopers.application.cart.CartItemFactory;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public Cart addToCart(CartCommand command) {
        User user = userRepository.findByUserId(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + command.userId()));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.create(user)));

        List<Long> productIds = command.items().stream()
                .map(CartItemCommand::productId)
                .toList();
        List<Product> products = productRepository.findAllById(productIds);

        cart.addItem(CartItemFactory.createFrom(command.items(), products));

        return cartRepository.save(cart);
    }

    public Cart getMyCart(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        return cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다: " + userId));
    }

}
