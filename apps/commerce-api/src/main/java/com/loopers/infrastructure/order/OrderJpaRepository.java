package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    Optional<Order> findByIdAndUser(Long id, User user);

    @EntityGraph(attributePaths = "orderItems.product")
    Optional<Order> findById(Long orderId);

}
