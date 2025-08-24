package com.loopers.domain.order;


import com.loopers.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    List<Order> findByUser(User user);

    Optional<Order> findByIdAndUser(Long orderId, User user);

    List<Order> findAll();

    Optional<Order> findById(Long orderId);

}
