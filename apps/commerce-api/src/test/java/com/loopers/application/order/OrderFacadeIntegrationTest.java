package com.loopers.application.order;

import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.*;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private User user;
    private Product product;
    private Coupon coupon;
    private UserCoupon userCoupon;
    private Point point;

    @BeforeEach
    void setUp() {
        user = userRepository.save(TestFixture.createUser());

        product = productRepository.save(TestFixture.createProduct(brandRepository.save(TestFixture.createBrand())));

        coupon = couponRepository.save(new Coupon("10% 할인 쿠폰", CouponType.RATE, 10, 10, ZonedDateTime.now().plusDays(1)));
        userCoupon = userCouponRepository.save(UserCoupon.create(user.getUserId(), coupon.getId(), coupon.getExpiredAt()));

        point = pointRepository.save(Point.create(user).charge(5000L));
    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
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

            // 1. 쿠폰 사용 상태 확인
            UserCoupon usedCoupon = userCouponRepository.findByUserIdAndCouponId(user.getUserId(), coupon.getId()).orElseThrow();
            assertThat(usedCoupon.getStatus()).isEqualTo(UserCouponStatus.USED);
            assertThat(usedCoupon.getUsedAt()).isNotNull();

            // 2. 재고 차감 확인 (기존 10개 - 2개 주문 = 8개)
            Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(8);

            // 3. 포인트 차감 확인 (기존 5000 - 1800 = 3200)
            Point updatedPoint = pointRepository.findByUser(user);
            assertThat(updatedPoint.getBalance()).isEqualTo(3200L);

            // 4. 주문 상태 확인
            Optional<Order> savedOrder = orderRepository.findByIdAndUser(info.orderId(), user);
            assertThat(savedOrder).isPresent();
            assertThat(savedOrder.get().getStatus()).isEqualTo(OrderStatus.PAID);
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

            assertRollbackState(UserCouponStatus.AVAILABLE, point.getBalance());
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

            assertRollbackState(UserCouponStatus.AVAILABLE, point.getBalance());
        }

        @DisplayName("쿠폰이 사용 불가능 상태일 경우 주문은 실패하고 롤백된다.")
        @Test
        void fail_whenCouponIsNotAvailable() {
            // 쿠폰 상태를 USED로 변경 (이미 사용한 경우)
            userCoupon.use();
            userCouponRepository.save(userCoupon);

            OrderCommand command = buildValidCommand();

            assertThatThrownBy(() -> orderFacade.placeOrder(command))
                    .isInstanceOf(IllegalStateException.class);

            assertRollbackState(UserCouponStatus.USED, point.getBalance());
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

            assertRollbackState(UserCouponStatus.AVAILABLE, point.getBalance());
        }

        @DisplayName("포인트가 부족할 경우 주문은 실패하고 롤백된다.")
        @Test
        void fail_insufficientPoint() {
            // given
            point.use(4900L); // 남은 포인트: 100
            Point savedPoint = pointRepository.save(point);

            OrderCommand command = buildValidCommand();

            assertThatThrownBy(() -> orderFacade.placeOrder(command))
                    .isInstanceOf(IllegalStateException.class);

            assertRollbackState(UserCouponStatus.AVAILABLE, savedPoint.getBalance());
        }

        private void assertRollbackState(UserCouponStatus expectedCouponStatus, long expectedPointBalance) {
            // 주문이 저장되지 않아야 함
            List<Order> orders = orderRepository.findByUser(user);
            assertThat(orders).isEmpty();

            // 재고 차감 없어야 함
            Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(10);

            // 포인트 차감 없어야 함
            Point updatedPoint = pointRepository.findByUser(user);
            assertThat(updatedPoint.getBalance()).isEqualTo(expectedPointBalance);

            // 쿠폰 상태 검증
            UserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(user.getUserId(), coupon.getId()).orElse(null);
            if (userCoupon != null) {
                assertThat(userCoupon.getStatus()).isEqualTo(expectedCouponStatus);
                if (expectedCouponStatus == UserCouponStatus.AVAILABLE) {
                    assertThat(userCoupon.getUsedAt()).isNull();
                } else if (expectedCouponStatus == UserCouponStatus.USED) {
                    assertThat(userCoupon.getUsedAt()).isNotNull();
                }
            }
        }
    }

}
