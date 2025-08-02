package com.loopers.application.order;

import com.loopers.domain.order.OrderItem;
import com.loopers.domain.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderItemFactory {

    public static List<OrderItem> createFrom(
            List<OrderItemCommand> itemCommands,
            List<Product> products
    ) {

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return itemCommands.stream()
                .map(cmd -> {
                    Product product = productMap.get(cmd.productId());
                    if (product == null) {
                        throw new IllegalArgumentException("Product not found: " + cmd.productId());
                    }
                    return OrderItem.create(product, cmd.quantity(), cmd.price());
                })
                .toList();

    }

}
