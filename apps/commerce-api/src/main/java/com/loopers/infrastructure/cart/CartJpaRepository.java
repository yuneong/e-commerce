package com.loopers.infrastructure.cart;

import com.loopers.domain.cart.Cart;
import com.loopers.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CartJpaRepository extends JpaRepository<Cart, Long> {

    Cart findByUser(User user);

}
