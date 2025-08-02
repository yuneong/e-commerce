package com.loopers.infrastructure.cart;

import com.loopers.domain.cart.Cart;
import com.loopers.domain.cart.CartRepository;
import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;


@RequiredArgsConstructor
@Component
public class CartRepositoryImpl implements CartRepository {

    private final CartJpaRepository cartJpaRepository;

    @Override
    public Optional<Cart> findByUser(User user) {
        return Optional.ofNullable(cartJpaRepository.findByUser(user));
    }

    @Override
    public Cart save(Cart cart) {
        return cartJpaRepository.save(cart);
    }

}
