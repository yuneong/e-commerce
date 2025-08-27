package com.loopers.application.order;

import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.*;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.platform.MockDataPlatformSender;
import com.loopers.support.TestFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;


@SpringBootTest
class OrderFacadeIntegrationTest {

    @Autowired private OrderFacade orderFacade;
    @Autowired private UserRepository userRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private UserCouponRepository userCouponRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private DatabaseCleanUp databaseCleanUp;
    @MockitoBean private MockDataPlatformSender mockDataPlatformSender;

    private User user;
    private Product product;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        user = userRepository.save(TestFixture.createUser());

        product = productRepository.save(TestFixture.createProduct(brandRepository.save(TestFixture.createBrand())));

        coupon = couponRepository.save(new Coupon("10% 할인 쿠폰", CouponType.RATE, 10, 10, ZonedDateTime.now().plusDays(1)));
        userCouponRepository.save(UserCoupon.create(user.getUserId(), coupon.getId(), coupon.getExpiredAt()));

        mockDataPlatformSender.clear();
    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
        mockDataPlatformSender.clear();
    }

    private OrderCommand buildValidCommand() {
        return new OrderCommand(
                user.getUserId(),
                List.of(new OrderItemCommand(product.getId(), 2, 1000)),
                coupon.getId()
        );
    }

    @DisplayName("주문 성공")
    @Nested
    class Success {

        @DisplayName("모든 처리가 정상 반영된다.")
        @Test
        void success_placeOrder() {
            // when
            OrderInfo info = orderFacade.placeOrder(buildValidCommand());

            // then
            assertThat(info).isNotNull();
            assertThat(info.totalPrice()).isEqualTo(1800); // 2000 * 10% 할인

            // 재고 차감 확인 (기존 10개 - 2개 주문 = 8개)
            Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(8);

            // 주문 상태 확인
            Optional<Order> savedOrder = orderRepository.findByIdAndUser(info.orderId(), user);
            assertThat(savedOrder).isPresent();
            assertThat(savedOrder.get().getStatus()).isEqualTo(OrderStatus.PENDING);

            // 데이터 플랫폼 전송 확인
            verify(mockDataPlatformSender, timeout(1000)).sendOrderResult(
                    argThat(msg ->
                            msg.userId().equals(info.userId()) &&
                            msg.orderId().equals(info.orderId()) &&
                            msg.status().equals("PENDING")
                    )
            );
        }
    }

    @DisplayName("주문 실패")
    @Nested
    class Fail {

        @DisplayName("쿠폰이 존재하지 않으면 주문은 실패하고 롤백된다.")
        @Test
        void fail_whenCouponNotFound() {
            OrderCommand command = new OrderCommand(
                    user.getUserId(),
                    List.of(new OrderItemCommand(product.getId(), 2, 1000)),
                    9999L // 존재하지 않는 쿠폰 ID
            );

            assertThatThrownBy(() -> orderFacade.placeOrder(command))
                    .isInstanceOf(IllegalArgumentException.class);

            assertRollbackState();
        }

        @DisplayName("유저가 해당 쿠폰을 보유하지 않으면 주문은 실패하고 롤백된다.")
        @Test
        void fail_whenUserDoesNotOwnCoupon() {
            // 쿠폰은 존재하지만, 유저와 연결된 UserCoupon은 없음
            User anotherUser = userRepository.save(User.create("TestUser", Gender.M, "2000-01-01", "loopers@gamil.com"));
            OrderCommand command = new OrderCommand(
                    anotherUser.getUserId(),
                    List.of(new OrderItemCommand(product.getId(), 2, 1000)),
                    coupon.getId()
            );

            assertThatThrownBy(() -> orderFacade.placeOrder(command))
                    .isInstanceOf(IllegalArgumentException.class);

            assertRollbackState();
        }

        @DisplayName("재고가 부족할 경우 주문은 실패하고 롤백된다.")
        @Test
        void fail_insufficientStock() {
            // given
            OrderCommand command = new OrderCommand(
                    user.getUserId(),
                    List.of(new OrderItemCommand(product.getId(), 100, 1000)), // 재고 10개인데 100개 주문
                    coupon.getId()
            );

            assertThatThrownBy(() -> orderFacade.placeOrder(command))
                    .isInstanceOf(IllegalStateException.class);

            assertRollbackState();
        }

        private void assertRollbackState() {
            // 주문이 저장되지 않아야 함
            List<Order> orders = orderRepository.findByUser(user);
            assertThat(orders).isEmpty();

            // 재고 차감 없어야 함
            Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(10);
        }
    }

}
