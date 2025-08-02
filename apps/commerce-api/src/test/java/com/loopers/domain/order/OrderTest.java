package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @DisplayName("place()")
    @Nested
    class placeOrder {

        @DisplayName("정상 주문 생성에 성공한다.")
        @Test
        void success() {
            // given
            User user = new User();
            Product product = new Product();
            OrderItem item1 = OrderItem.create(product, 1, 1000);
            OrderItem item2 = OrderItem.create(product, 1, 2000);

            // when
            Order order = Order.place(user, List.of(item1, item2));

            // then
            assertAll(
                    () -> assertEquals(user, order.getUser()),
                    () -> assertEquals(2, order.getOrderItems().size()),
                    () -> assertEquals(3000, order.getTotalPrice()),
                    () -> assertEquals(OrderStatus.PLACED, order.getStatus()),
                    () -> assertNotNull(order.getPaidAt())
            );
        }

        @DisplayName("주문 아이템이 null이면 예외가 발생한다.")
        @Test
        void throwsException_whenItemsNull() {
            // given
            User user = new User();

            // when & then
            assertThrows(IllegalArgumentException.class, () -> Order.place(user, null));
        }

        @DisplayName("주문 사용자(User)가 null이면 예외가 발생한다.")
        @Test
        void throwsException_whenUserIsNull() {
            // given
            Product product = new Product();
            OrderItem item = OrderItem.create(product, 1, 1000);

            // when & then
            assertThrows(NullPointerException.class, () -> Order.place(null, List.of(item)));
        }

        @DisplayName("주문 아이템이 빈 리스트이면 예외가 발생한다.")
        @Test
        void throwsException_whenItemsEmpty() {
            // given
            User user = new User();

            // when & then
            assertThrows(IllegalArgumentException.class, () -> Order.place(user, List.of()));
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
            Product product = new Product();
            OrderItem item = OrderItem.create(product, 1, 1500);

            // when
            order.addItem(item);

            // then
            assertEquals(1, order.getOrderItems().size());
            assertEquals(item, order.getOrderItems().get(0));
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


    @DisplayName("calculateTotalPrice()")
    @Nested
    class CalculateTotalPrice {

        @DisplayName("주문 아이템들의 가격 합계를 계산한다.")
        @Test
        void success() {
            // given
            Order order = new Order();
            Product product = new Product();
            OrderItem item1 = OrderItem.create(product, 1, 1000);
            OrderItem item2 = OrderItem.create(product, 1, 2000);
            order.addItem(item1);
            order.addItem(item2);

            // when
            int total = order.calculateTotalPrice();

            // then
            assertEquals(3000, total);
        }

        @DisplayName("주문 아이템이 없으면 총합은 0이다.")
        @Test
        void returnsZero_whenNoItems() {
            // given
            Order order = new Order();

            // when
            int total = order.calculateTotalPrice();

            // then
            assertEquals(0, total);
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
