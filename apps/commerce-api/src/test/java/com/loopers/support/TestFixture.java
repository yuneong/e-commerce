package com.loopers.support;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;

import java.util.ArrayList;
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

    public static User createUser(String userId) {
        return User.create(
                userId,
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

    public static List<Product> createProductList(Brand brand, int count) {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            products.add(Product.create(
                    brand,
                    "나이키조던" + (i + 1),
                    "운동화",
                    "https://example.com/logo.png",
                    1000,
                    10
            ));
        }
        return products;
    }

    public static List<OrderItem> createOrderItems(Product product, int quantity) {
        OrderItem item1 = OrderItem.create(product, quantity, product.getPrice());
        return List.of(item1);
    }

    public static List<OrderItem> createOrderItems(List<Product> products, int quantity) {
        List<OrderItem> items = new ArrayList<>();
        for (Product product : products) {
            items.add(OrderItem.create(product, quantity, product.getPrice()));
        }
        return items;
    }

    public static Order createOrder(User user, List<OrderItem> orderItems, int totalPrice, Long couponId) {
        return Order.place(user, orderItems, totalPrice, couponId);
    }

//    /**
//     * 주문 + 아이템 N개 + (선택) 쿠폰까지 한 번에 구성
//     * - 할인 로직은 테스트 단순화를 위해 "할인 없음(=총액 그대로)"로 계산.
//     * - 쿠폰을 쓰지 않으면 couponId=null 로 전달.
//     */
//    public static Order createOrderWithItemsAndOptionalCoupon(
//            User user,
//            List<Product> products,
//            List<Integer> quantities,
//            Long couponIdOrNull
//    ) {
//        if (products.size() != quantities.size()) {
//            throw new IllegalArgumentException("products와 quantities 길이가 다릅니다.");
//        }
//
//        // 1) OrderItem 목록 구성
//        List<OrderItem> items = new ArrayList<>();
//        for (int i = 0; i < products.size(); i++) {
//            Product p = products.get(i);
//            int qty = quantities.get(i);
//
//            // 프로젝트에 있는 정적 팩토리를 그대로 사용
//            OrderItem item = OrderItem.create(p, qty, p.getPrice());
//            items.add(item);
//        }
//
//        // 2) 총액 계산 (단가 * 수량)
//        int totalPrice = items.stream()
//                .mapToInt(OrderItem::getPrice)
//                .sum();
//
//        // 3) 최종 주문 생성 (Order.place 내부에서 order.addItem(item) 호출됨)
//        return Order.place(user, items, totalPrice, couponIdOrNull);
//    }
//
//    // 상품 1개 + 수량 지정, 쿠폰은 없음
//    public static Order createOrder(User user, Product product, int quantity) {
//        return createOrderWithItemsAndOptionalCoupon(
//                user,
//                List.of(product),
//                List.of(quantity),
//                null
//        );
//    }
//
//    // 상품 1개 + 수량 지정 + 쿠폰 ID
//    public static Order createOrder(User user, Product product, int quantity, Long couponId) {
//        return createOrderWithItemsAndOptionalCoupon(
//                user,
//                List.of(product),
//                List.of(quantity),
//                couponId
//        );
//    }
//
//    // 상품 여러 개, 수량은 전부 1, 쿠폰 없음
//    public static Order createOrder(User user, List<Product> products) {
//        List<Integer> quantities = products.stream()
//                .map(p -> 1) // 전부 수량 1
//                .toList();
//
//        return createOrderWithItemsAndOptionalCoupon(user, products, quantities, null);
//    }



}
