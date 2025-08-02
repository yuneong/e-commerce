package com.loopers.domain.cart;


import com.loopers.domain.user.User;

import java.util.Optional;

public interface CartRepository {

    Optional<Cart> findByUser(User user);

    Cart save(Cart cart);

}
