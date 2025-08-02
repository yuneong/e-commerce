package com.loopers.domain.cart;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    @DisplayName("CartItem 생성 시,")
    @Nested
    class CreateCartItem {

        Brand dummyBrand = new Brand();

        @DisplayName("성공한다.")
        @Test
        void success_createCartItem() {
            // given
            Product product = Product.create(
                    dummyBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    1000,
                    10
            );
            int quantity = 2;
            int price = 1000;

            // when
            CartItem cartItem = CartItem.create(product, quantity, price);

            // then
            assertAll(
                    () -> assertEquals(product, cartItem.getProduct()),
                    () -> assertEquals(quantity, cartItem.getQuantity()),
                    () -> assertEquals(price, cartItem.getPrice())
            );
        }

        @DisplayName("상품 수량이 0 이하이면 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        void throwsIllegalArgumentException_whenQuantityIsNegative(int quantity) {
            // given
            Product product = Product.create(
                    dummyBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    1000,
                    10
            );

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    CartItem.create(product, quantity, 1000)
            );
        }

        @DisplayName("가격이 0 이하이면 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        void throwsIllegalArgumentException_whenPriceIsNegative(int price) {
            // given
            Product product = Product.create(
                    dummyBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    1000,
                    10
            );

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    CartItem.create(product, 1, price)
            );
        }

        @DisplayName("상품이 null이면 예외가 발생한다.")
        @Test
        void throwsNullPointerException_whenProductIsNull() {
            assertThrows(NullPointerException.class, () ->
                    CartItem.create(null, 1, 1000)
            );
        }
    }
}
