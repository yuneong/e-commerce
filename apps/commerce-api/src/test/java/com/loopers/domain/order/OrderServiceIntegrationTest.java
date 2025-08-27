package com.loopers.domain.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.TestFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

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
        User user = TestFixture.createUser();
        savedUser = userRepository.save(user);

        Brand brand = TestFixture.createBrand();
        savedBrand = brandRepository.save(brand);
    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문 생성 시,")
    @Nested
    class createOrder {

        @DisplayName("정상적으로 주문이 생성된다.")
        @Test
        void createOrder_success() {
            // given
            Product product = Product.create(
                    savedBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    10000,
                    10
            );
            Product savedProduct = productRepository.save(product);
            List<OrderItem> items = TestFixture.createOrderItems(savedProduct, 2); // savedProduct의 수량 2개
            int itemsPrice = items.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();
            int discountAmount = 2000;
            int totalPrice = Math.max(0, itemsPrice - discountAmount);

            // when
            Order order = orderService.createOrder(savedUser, items, totalPrice);

            // then
            assertAll(
                    () -> assertThat(order.getOrderItems()).hasSize(1),
                    () -> assertThat(order.getTotalPrice()).isEqualTo(18000)
            );
        }

        @DisplayName("user가 null이면 예외가 발생한다.")
        @Test
        void createOrder_fail_dueToNullUser() {
            // given
            Product product = Product.create(
                    savedBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    10000,
                    10
            );
            Product savedProduct = productRepository.save(product);
            List<OrderItem> items = TestFixture.createOrderItems(savedProduct, 2);

            // when & then
            assertThrows(NullPointerException.class, () -> {
                orderService.createOrder(null, items, 2000);
            });
        }

        @DisplayName("주문 아이템이 비어있으면 예외가 발생한다.")
        @Test
        void createOrder_fail_dueToEmptyOrderItems() {
            // given
            Product product = Product.create(
                    savedBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    10000,
                    10
            );
            Product savedProduct = productRepository.save(product);
            List<OrderItem> items = List.of();

            // when & then
            assertThrows(NullPointerException.class, () -> {
                orderService.createOrder(savedUser, items, 2000);
            });
        }

        @DisplayName("총 금액이 음수이면 예외가 발생한다.")
        @Test
        void createOrder_fail_dueToNegativeTotalPrice() {
            // given
            Product product = Product.create(
                    savedBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    10000,
                    10
            );
            Product savedProduct = productRepository.save(product);
            List<OrderItem> items = TestFixture.createOrderItems(savedProduct, 2);

            // when & then
            assertThrows(IllegalArgumentException.class, () -> {
                orderService.createOrder(savedUser, items, -1000);
            });
        }
    }

    @DisplayName("주문 목록 조회 시,")
    @Nested
    class getOrders {

        @DisplayName("주문 목록을 정상적으로 조회한다.")
        @Test
        void success() {
            // given
            Product product = Product.create(
                    savedBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    10000,
                    10
            );
            Product savedProduct = productRepository.save(product);
            List<OrderItem> items = TestFixture.createOrderItems(savedProduct, 1);
            orderService.createOrder(savedUser, items, 2000);

            // when
            List<Order> orders = orderService.getOrders(savedUser);

            // then
            assertThat(orders).isNotEmpty();
            assertThat(orders.get(0).getUser().getId()).isEqualTo(savedUser.getId());
        }

        @DisplayName("주문이 없는 경우 빈 목록을 반환한다.")
        @Test
        void emptyOrders() {
            // when
            List<Order> orders = orderService.getOrders(savedUser);

            // then
            assertThat(orders).isEmpty();
        }

    }

    @DisplayName("주문 상세 조회 시,")
    @Nested
    class getOrderDetails {

        @DisplayName("주문 상세를 정상적으로 조회한다.")
        @Test
        void success() {
            // given
            Product product = Product.create(
                    savedBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    10000,
                    10
            );
            Product savedProduct = productRepository.save(product);
            List<OrderItem> items = TestFixture.createOrderItems(savedProduct, 1);

            Order order = orderService.createOrder(savedUser, items, 2000);

            // when
            Order found = orderService.getOrderDetail(order.getId(), savedUser);

            // then
            assertThat(found.getId()).isEqualTo(order.getId());
        }

        @DisplayName("존재하지 않는 주문 ID로 조회하면 null을 반환한다.(예외 발생)")
        @Test
        void orderNotFound() {
            // when & then
            assertThrows(IllegalArgumentException.class, () -> {
                orderService.getOrderDetail(-1L, savedUser);
            });
        }

        @DisplayName("다른 유저의 주문을 조회하면 null을 반환한다.(예외 발생)")
        @Test
        void wrongUser() {
            // given
            User savedUser2 = userRepository.save(User.create("oyy2", Gender.F, "1999-01-01", "loopers@gmail.com"));

            Product product = Product.create(
                    savedBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    10000,
                    10
            );
            Product savedProduct = productRepository.save(product);
            List<OrderItem> items = TestFixture.createOrderItems(savedProduct, 1);

            Order order = orderService.createOrder(savedUser, items, 2000);

            // when & then
            assertThrows(IllegalArgumentException.class, () -> {
                orderService.getOrderDetail(order.getId(), savedUser2);
            });
        }

    }

}
