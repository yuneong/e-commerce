package com.loopers.domain.order;

import com.loopers.application.order.OrderItemCommand;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
    private PointRepository pointRepository;

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

    @DisplayName("주문 생성 시,")
    @Nested
    class createOrder {

        @DisplayName("정상적으로 주문이 생성되고 재고 및 포인트가 차감된다.")
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

            Point point = Point.create(savedUser);
            point.charge(50000);
            Point savedPoint = pointRepository.save(point);

            OrderItemCommand command = new OrderItemCommand(savedProduct.getId(), 2, 10000);
            List<OrderItemCommand> commands = List.of(command);

            // when
            Order order = orderService.createOrder(commands, savedUser, savedPoint, List.of(savedProduct));

            // then
            assertAll(
                    () -> assertThat(order.getOrderItems()).hasSize(1),
                    () -> assertThat(order.getTotalPrice()).isEqualTo(20000),
                    () -> assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID),
                    () -> assertThat(savedProduct.getStock()).isEqualTo(8),
                    () -> assertThat(savedPoint.getBalance()).isEqualTo(30000)
            );
        }

        @DisplayName("재고가 부족하면 예외가 발생한다.")
        @Test
        void createOrder_fail_dueToInsufficientStock() {
            // given
            Product product = Product.create(
                    savedBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    10000,
                    1
            );
            Product savedProduct = productRepository.save(product);

            Point point = Point.create(savedUser);
            point.charge(50000);
            Point savedPoint = pointRepository.save(point);

            OrderItemCommand command = new OrderItemCommand(savedProduct.getId(), 2, 10000);
            List<OrderItemCommand> commands = List.of(command);

            // when & then
            assertThrows(IllegalStateException.class, () -> {
                orderService.createOrder(commands, savedUser, savedPoint, List.of(savedProduct));
            });
        }

        @DisplayName("포인트가 부족하면 예외가 발생한다.")
        @Test
        void createOrder_fail_dueToInsufficientPoint() {
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

            Point point = Point.create(savedUser);
            point.charge(20000);
            Point savedPoint = pointRepository.save(point);

            OrderItemCommand command = new OrderItemCommand(savedProduct.getId(), 5, 10000);
            List<OrderItemCommand> commands = List.of(command);

            // when & then
            assertThrows(IllegalStateException.class, () -> {
                orderService.createOrder(commands, savedUser, savedPoint, List.of(savedProduct));
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

            Point point = Point.create(savedUser);
            point.charge(20000);
            Point savedPoint = pointRepository.save(point);

            OrderItemCommand command = new OrderItemCommand(savedProduct.getId(), 1, 10000);
            orderService.createOrder(List.of(command), savedUser, savedPoint, List.of(savedProduct));

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

            Point point = Point.create(savedUser);
            point.charge(20000);
            Point savedPoint = pointRepository.save(point);

            OrderItemCommand command = new OrderItemCommand(savedProduct.getId(), 1, 10000);
            Order order = orderService.createOrder(List.of(command), savedUser, savedPoint, List.of(savedProduct));

            // when
            Order found = orderService.getOrderDetail(order.getId(), savedUser);

            // then
            assertThat(found.getId()).isEqualTo(order.getId());
        }

        @DisplayName("존재하지 않는 주문 ID로 조회하면 null을 반환한다.")
        @Test
        void orderNotFound() {
            // when
            Order found = orderService.getOrderDetail(-1L, savedUser);

            // then
            assertThat(found).isNull();
        }

        @DisplayName("다른 유저의 주문을 조회하면 null을 반환한다.")
        @Test
        void wrongUser() {
            // given
            User savedUser2 = userRepository.save(new User());

            Product product = Product.create(
                    savedBrand,
                    "상품명",
                    "상품설명",
                    "https://example.com/image.jpg",
                    10000,
                    10
            );
            Product savedProduct = productRepository.save(product);

            Point point = Point.create(savedUser);
            point.charge(20000);
            Point savedPoint = pointRepository.save(point);

            OrderItemCommand command = new OrderItemCommand(savedProduct.getId(), 1, 10000);
            Order order = orderService.createOrder(List.of(command), savedUser, savedPoint, List.of(savedProduct));

            // when
            Order found = orderService.getOrderDetail(order.getId(), savedUser2);

            // then
            assertThat(found).isNull();
        }

    }

}
