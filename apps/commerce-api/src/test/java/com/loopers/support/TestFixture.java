package com.loopers.support;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;

import java.util.List;

public class TestFixture {

    public static User createUser() {
        return User.create(
                "oyy",
                Gender.F,
                "1999-08-21",
                "loopers@gmail.com"
        );
    }

    public static Brand createBrand() {
        return Brand.create(
                "나이키",
                "스포츠브랜드",
                "https://example.com/logo.png"
        );
    }

    public static Product createProduct(Brand brand) {
        return Product.create(
                brand,
                "나이키조던",
                "운동화",
                "https://example.com/logo.png",
                1000,
                10
        );
    }


    public static List<OrderItem> createOrderItems(Product product, int quantity) {
        OrderItem item1 = OrderItem.create(product, quantity, product.getPrice());
        return List.of(item1);

//        OrderItem item2 = OrderItem.create(product, 1, product.getPrice());
//        OrderItem item3 = OrderItem.create(product, 1, product.getPrice());
//        return List.of(item1, item2, item3);
    }

}
