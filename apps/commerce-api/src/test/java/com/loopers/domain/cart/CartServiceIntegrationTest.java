package com.loopers.domain.cart;

import com.loopers.application.cart.CartCommand;
import com.loopers.application.cart.CartItemCommand;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private User savedUser;

    private Brand savedBrand;

    @BeforeEach
    void setUp() {
        User user = new User();
        savedUser = userRepository.save(user);

        Brand brand = new Brand();
        savedBrand = brandRepository.save(brand);
    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("장바구니 담기 시,")
    @Nested
    class AddToCart {

        @DisplayName("정상적으로 담기면 장바구니가 반환된다.")
        @Test
        void success() {
            // given
            Product product = Product.create(
                    savedBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    1000,
                    10
            );
            Product savedProduct = productRepository.save(product);

            CartItemCommand itemCommand = new CartItemCommand(
                    savedProduct.getId(),
                    2,
                    1000
            );
            CartCommand command = new CartCommand(savedUser.getUserId(), List.of(itemCommand));

            // when
            Cart cart = cartService.addToCart(command);

            // then
            assertAll(
                    () -> assertThat(cart.getUser().getId()).isEqualTo(savedUser.getId()),
                    () -> assertThat(cart.getCartItems()).hasSize(1),
                    () -> assertThat(cart.getTotalPrice()).isEqualTo(2000)
            );
        }

        @DisplayName("유저가 존재하지 않으면 예외가 발생한다.")
        @Test
        void fail_userNotFound() {
            // given
            CartItemCommand itemCommand = new CartItemCommand(1L, 2, 1000);
            CartCommand command = new CartCommand("unknown_user", List.of(itemCommand));

            // when & then
            assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(command));
        }
    }

    @DisplayName("내 장바구니 조회 시,")
    @Nested
    class GetMyCart {

        @DisplayName("정상적으로 조회된다.")
        @Test
        @Transactional
        void success() {
            // given
            Product product = Product.create(
                    savedBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    1000,
                    5
            );
            Product savedProduct = productRepository.save(product);

            CartItemCommand itemCommand = new CartItemCommand(
                    savedProduct.getId(),
                    1,
                    1000
            );
            CartCommand command = new CartCommand(savedUser.getUserId(), List.of(itemCommand));
            cartService.addToCart(command);

            // when
            Cart cart = cartService.getMyCart(savedUser.getUserId());

            // then
            assertThat(cart).isNotNull();
            assertThat(cart.getCartItems()).hasSize(1);
        }

        @DisplayName("유저가 존재하지 않으면 예외가 발생한다.")
        @Test
        void fail_userNotFound() {
            assertThrows(IllegalArgumentException.class, () -> cartService.getMyCart("no_user"));
        }

        @DisplayName("장바구니가 존재하지 않으면 예외가 발생한다.")
        @Test
        void fail_cartNotFound() {
            assertThrows(IllegalArgumentException.class, () -> cartService.getMyCart(savedUser.getUserId()));
        }
    }

}
