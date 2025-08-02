package com.loopers.domain.cart;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    @DisplayName("create()")
    @Nested
    class CreateCart {

        @DisplayName("사용자 정보가 있으면 장바구니 생성에 성공한다.")
        @Test
        void success() {
            // given
            User user = new User();

            // when
            Cart cart = Cart.create(user);

            // then
            assertNotNull(cart);
            assertNotNull(cart.getId());
            assertEquals(user, cart.getUser());
        }

        @DisplayName("사용자 정보가 null이면 예외가 발생한다.")
        @Test
        void failWhenUserIsNull() {
            assertThrows(NullPointerException.class, () -> Cart.create(null));
        }
    }


    @DisplayName("addItem()")
    @Nested
    class AddItem {

        Brand dummyBrand = new Brand();
        Product product = Product.create(
                dummyBrand,
                "상품명",
                "상품설명",
                "https://example.com/image.jpg",
                1000,
                10
        );

        @DisplayName("장바구니 아이템 추가에 성공한다.")
        @Test
        void success() {
            // given
            Cart cart = Cart.create(new User());
            CartItem item1 = CartItem.create(product, 2, 1000);
            CartItem item2 = CartItem.create(product, 1, 2000);

            // when
            cart.addItem(List.of(item1, item2));

            // then
            assertAll(
                    () -> assertEquals(2, cart.getCartItems().size()),
                    () -> assertEquals(1000 * 2 + 2000 * 1, cart.getTotalPrice())
            );
        }

        @DisplayName("null 또는 빈 리스트를 추가하면 예외가 발생한다.")
        @ParameterizedTest
        @NullAndEmptySource
        void failWhenInvalidList(List<CartItem> input) {
            // given & when
            Cart cart = Cart.create(new User());

            // then
            assertThrows(IllegalArgumentException.class, () -> cart.addItem(input));
        }

        @DisplayName("null 아이템이 포함되어 있으면 예외가 발생한다.")
        @Test
        void failWhenItemIsNull() {
            // given
            Cart cart = Cart.create(new User());
            CartItem item = null;

            // when & then
            assertThrows(NullPointerException.class, () -> cart.addItem(List.of(item)));
        }
    }

}
