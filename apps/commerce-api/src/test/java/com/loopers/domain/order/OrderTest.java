package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.support.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
            DiscountedOrderByCoupon discountedOrderByCoupon = new DiscountedOrderByCoupon(
                    1L,
                    BigDecimal.valueOf(1500) // 할인 후 가격 1500원
            );

            // when
            Order order = Order.place(user, List.of(item1, item2), discountedOrderByCoupon);

            // then
            assertAll(
                    () -> assertEquals(user, order.getUser()),
                    () -> assertEquals(2, order.getOrderItems().size()),
                    () -> assertEquals(BigDecimal.valueOf(1500), order.getTotalPrice()),
                    () -> assertEquals(OrderStatus.CREATED, order.getStatus()),
                    () -> assertNotNull(order.getPaidAt())
            );
        }

        @DisplayName("주문 아이템이 null이면 예외가 발생한다.")
        @Test
        void throwsException_whenItemsNull() {
            // given
            DiscountedOrderByCoupon discountedOrderByCoupon = new DiscountedOrderByCoupon(
                    1L,
                    BigDecimal.valueOf(1500) // 할인 후 가격 1500원
            );

            // when & then
            assertThrows(IllegalArgumentException.class, () -> Order.place(user, null, discountedOrderByCoupon));
        }

        @DisplayName("주문 사용자(User)가 null이면 예외가 발생한다.")
        @Test
        void throwsException_whenUserIsNull() {
            // given
            OrderItem item = OrderItem.create(product, 1, 1000);
            DiscountedOrderByCoupon discountedOrderByCoupon = new DiscountedOrderByCoupon(
                    1L,
                    BigDecimal.valueOf(500) // 할인 후 가격 500원
            );

            // when & then
            assertThrows(NullPointerException.class, () -> Order.place(null, List.of(item), discountedOrderByCoupon));
        }

        @DisplayName("주문 아이템이 빈 리스트이면 예외가 발생한다.")
        @Test
        void throwsException_whenItemsEmpty() {
            // given
            DiscountedOrderByCoupon discountedOrderByCoupon = new DiscountedOrderByCoupon(
                    1L,
                    BigDecimal.valueOf(0) // 할인 후 가격 0원
            );

            // when & then
            assertThrows(IllegalArgumentException.class, () -> Order.place(user, List.of(), discountedOrderByCoupon));
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
