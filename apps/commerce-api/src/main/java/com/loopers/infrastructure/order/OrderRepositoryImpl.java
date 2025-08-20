package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public List<Order> findByUser(User user) {
        return orderJpaRepository.findByUser(user);
    }

    @Override
    public Optional<Order> findByIdAndUser(Long orderId, User user) {
        return orderJpaRepository.findByIdAndUser(orderId, user);
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll();
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJpaRepository.findById(orderId);
    }

}
