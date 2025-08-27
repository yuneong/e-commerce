package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.support.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    User user = TestFixture.createUser();
    Product product = TestFixture.createProduct(TestFixture.createBrand());

    @DisplayName("place()")
    @Nested
    class placeOrder {

        @DisplayName("정상 주문 생성에 성공한다.")
        @Test
        void success() {
            // given
            OrderItem item1 = OrderItem.create(product, 1, 1000);
            OrderItem item2 = OrderItem.create(product, 1, 2000);
            List<OrderItem> items = List.of(item1, item2);
            int itemsPrice = items.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();
            int discountAmount = 500;
            int totalPrice = Math.max(0, itemsPrice - discountAmount);

            // when
            Order order = Order.place(user, items, totalPrice);

            // then
            assertAll(
                    () -> assertEquals(user, order.getUser()),
                    () -> assertEquals(2, order.getOrderItems().size()),
                    () -> assertEquals(2500, order.getTotalPrice()),
                    () -> assertEquals(OrderStatus.INIT, order.getStatus()),
                    () -> assertNotNull(order.getPaidAt())
            );
        }

        @DisplayName("주문 아이템이 null이면 예외가 발생한다.")
        @Test
        void throwsException_whenItemsNull() {
            // when & then
            assertThrows(NullPointerException.class, () -> Order.place(user, null, 500));
        }

        @DisplayName("주문 사용자(User)가 null이면 예외가 발생한다.")
        @Test
        void throwsException_whenUserIsNull() {
            // given
            OrderItem item = OrderItem.create(product, 1, 1000);
            List<OrderItem> items = List.of(item);
            int itemsPrice = items.stream().mapToInt(i -> i.getPrice() * i.getQuantity()).sum();
            int discountAmount = 500;
            int totalPrice = Math.max(0, itemsPrice - discountAmount);

            // when & then
            assertThrows(NullPointerException.class, () -> Order.place(null, items, totalPrice));
        }

        @DisplayName("주문 아이템이 빈 리스트이면 예외가 발생한다.")
        @Test
        void throwsException_whenItemsEmpty() {
            // when & then
            assertThrows(NullPointerException.class, () -> Order.place(user, List.of(), 0));
        }
    }

    @DisplayName("updateOrder()")
    @Nested
    class UpdateOrder {
        @DisplayName("주문 정보를 정상적으로 수정한다.")
        @Test
        void success() {
            // given
            Order order = new Order();
            int newTotalPrice = 2000;
            Long newCouponId = 2L;

            // when
            order.updateOrder(newTotalPrice, newCouponId);

            // then
            assertEquals(newTotalPrice, order.getTotalPrice());
            assertEquals(OrderStatus.PENDING, order.getStatus());
            assertEquals(newCouponId, order.getCouponId());
        }
    }


    @DisplayName("addItem()")
    @Nested
    class AddItem {

        @DisplayName("아이템을 정상적으로 추가한다.")
        @Test
        void success() {
            // given
            Order order = new Order();
            OrderItem item = OrderItem.create(product, 1, 1500);

            // when
            order.addItem(item);

            // then
            assertEquals(1, order.getOrderItems().size());
            assertEquals(item, order.getOrderItems().getFirst());
        }

        @DisplayName("아이템이 null이면 예외가 발생한다.")
        @Test
        void throwsException_whenItemIsNull() {
            // given
            Order order = new Order();

            // when & then
            assertThrows(IllegalArgumentException.class, () -> order.addItem(null));
        }

    }

    @DisplayName("updateOrderStatus()")
    @Nested
    class UpdateOrderStatus {

        @DisplayName("주문 상태를 정상적으로 변경한다.")
        @Test
        void success() {
            // given
            Order order = new Order();

            // when
            order.updateOrderStatus(OrderStatus.PAID);

            // then
            assertEquals(OrderStatus.PAID, order.getStatus());
        }

        @DisplayName("null 상태로 업데이트하면 예외가 발생한다.")
        @Test
        void throwsException_whenStatusIsNull() {
            Order order = new Order();

            assertThrows(IllegalArgumentException.class, () -> order.updateOrderStatus(null));
        }
    }

}
