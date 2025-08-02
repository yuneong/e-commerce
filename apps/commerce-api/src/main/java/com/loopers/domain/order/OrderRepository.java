package com.loopers.domain.order;


import com.loopers.domain.user.User;

import java.util.List;

public interface OrderRepository {

    Order save(Order order);

    List<Order> findByUser(User user);

    Order findByIdAndUser(Long orderId, User user);

}
