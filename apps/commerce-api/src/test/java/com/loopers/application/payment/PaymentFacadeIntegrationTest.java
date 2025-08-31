package com.loopers.application.payment;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.*;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.*;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.event.PaymentSucceededEvent;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.support.TestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class PaymentFacadeIntegrationTest {

    @Autowired private PaymentFacade paymentFacade;
    @Autowired private OrderService orderService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PointService realPointService;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private UserCouponRepository userCouponRepository;

    @MockitoSpyBean private PaymentService paymentService;   // 카드 결제 동작 통제
    @MockitoSpyBean private PointService pointService;       // 포인트 실패 케이스 유도
    @MockitoSpyBean private CouponService couponService;     // 성공 이벤트 후 쿠폰 사용 호출 검증
    @MockitoSpyBean private ProductService productService;   // 실패 이벤트 후 재고 원복 호출 검증


    private User setupUser(String userId) {
        return userRepository.save(TestFixture.createUser(userId));
    }

    private Point setupPoint(User user, long amount) {
        realPointService.create(user);
        return realPointService.charge(user, amount);
    }

    private UserCoupon setupCoupon(String userId) {
        Coupon coupon = couponRepository.save(new Coupon("1000원 할인 쿠폰", CouponType.FIXED, 10, 1000, ZonedDateTime.now()));
        return userCouponRepository.save(new UserCoupon(userId, coupon.getId(), UserCouponStatus.AVAILABLE, null, ZonedDateTime.now().plusDays(2)));
    }

    private Order setupOrder(String userId, int totalPrice) {
        Brand brand = brandRepository.save(TestFixture.createBrand());
        List<Product> products = productRepository.saveAll(TestFixture.createProductList(brand, 2));
        List<OrderItem> orderItems = TestFixture.createOrderItems(products, 1);
        User user = userService.getMyInfo(userId);
        return orderService.createOrder(user, orderItems, totalPrice, null);
    }

    private Order setupOrderWithCoupon(String userId, int amount, Long couponId) {
        Brand brand = brandRepository.save(TestFixture.createBrand());
        List<Product> products = productRepository.saveAll(TestFixture.createProductList(brand, 2));
        List<OrderItem> orderItems = TestFixture.createOrderItems(products, 1);
        User user = userService.getMyInfo(userId);
        return orderService.createOrder(user, orderItems, amount, couponId);
    }

    private ProcessPaymentCommand buildCmd(
            String userId,
            Long orderId,
            int amount,
            PaymentMethod method,
            String cardNo,
            CardType cardType
    ) {
        return new ProcessPaymentCommand(userId, orderId, amount, method, cardNo, cardType);
    }

    @Nested
    @DisplayName("processPayment() - 전략별 성공/실패")
    class ProcessPayment {

        @Test
        @DisplayName("CARD 성공 - requestAndSavePayment 성공 시 SUCCESS 반환")
        void card_success() {
            // given
            String userId = "oyyCardOk";
            User user = setupUser(userId);
            Order order = setupOrder(userId, 10000);
            ProcessPaymentCommand cmd = buildCmd(user.getUserId(), order.getId(), 10000, PaymentMethod.CARD, "1234-5678-9012-3456", CardType.SAMSUNG);

            doAnswer(invocation -> {
                Payment p = invocation.getArgument(0);
                p.updateStatus(PaymentStatus.SUCCESS, null);
                if (p.getCardDetail() != null) {
                    p.getCardDetail().withTransactionKey("TX-12345");
                }
                return p;
            }).when(paymentService).requestAndSavePayment(any(Payment.class), anyString());

            // when
            PaymentInfo result = paymentFacade.processPayment(cmd);

            // then
            assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
            verify(paymentService, times(1)).requestAndSavePayment(any(Payment.class), anyString());
        }

        @Test
        @DisplayName("CARD 실패 - requestAndSavePayment 실패 시 FAILED 반환")
        void card_failure() {
            // given
            String userId = "oyyCardNo";
            User user = setupUser(userId);
            Order order = setupOrder(userId, 20000);
            ProcessPaymentCommand cmd = buildCmd(user.getUserId(), order.getId(), 20000, PaymentMethod.CARD, "1234-5678-9012-3456", CardType.SAMSUNG);

            doAnswer(invocation -> {
                Payment p = invocation.getArgument(0);
                p.updateStatus(PaymentStatus.FAILED, "PG_ERROR");
                return p;
            }).when(paymentService).requestAndSavePayment(any(Payment.class), anyString());

            // when
            PaymentInfo result = paymentFacade.processPayment(cmd);

            // then
            assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(result.reason()).isEqualTo("PG_ERROR");
            verify(paymentService, times(1)).requestAndSavePayment(any(Payment.class), anyString());
        }

        @Test
        @DisplayName("POINT 성공 - 충분한 포인트 시 SUCCESS 및 savePayment 호출")
        void point_success() {
            // given
            String userId = "oyyPointOk";
            User user = setupUser(userId);
            setupPoint(user, 30000);
            Order order = setupOrder(userId, 15000);
            ProcessPaymentCommand cmd = buildCmd(user.getUserId(), order.getId(), 15000, PaymentMethod.POINT, "", CardType.NONE);

            // when
            PaymentInfo result = paymentFacade.processPayment(cmd);

            // then
            assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
            verify(paymentService, times(1)).savePayment(any(Payment.class)); // PointPaymentStrategy finally 블록
        }

        @Test
        @DisplayName("POINT 실패 - 포인트 부족 등 CoreException 시 FAILED 및 savePayment 호출")
        void point_failure() {
            // given
            String userId = "oyyPointNo";
            User user = setupUser(userId);
            setupPoint(user, 10000); // 포인트 부족하게 세팅
            Order order = setupOrder(userId, 40000);
            ProcessPaymentCommand cmd = buildCmd(user.getUserId(), order.getId(), 40000, PaymentMethod.POINT, "", CardType.NONE);

            doThrow(new IllegalStateException("포인트가 부족합니다."))
                    .when(pointService)
                    .checkAndUsePoint(any(Point.class), eq(40000));

            // when
            PaymentInfo result = paymentFacade.processPayment(cmd);

            // then
            assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(result.reason()).isEqualTo("포인트가 부족합니다.");
            verify(paymentService, times(1)).savePayment(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("결제 이벤트 후속 처리 - handlePaymentSucceeded/Failed")
    class EventHandlers {

        @Test
        @DisplayName("handlePaymentSucceeded: 주문을 PAID로 변경하고 쿠폰 사용 처리")
        void handlePaymentSucceeded_updates_order_and_uses_coupon() {
            // given
            String userId = "oyySuccess";
            User user = setupUser(userId);
            UserCoupon userCoupon = setupCoupon(userId);
            Order order = setupOrderWithCoupon(userId, 25000, userCoupon.getId());

            assertThat(order.getStatus()).isNotEqualTo(OrderStatus.PAID);

            PaymentSucceededEvent event = new PaymentSucceededEvent(
                    order.getId(), user.getUserId(), 9999L
            );

            // when
            paymentFacade.handlePaymentSucceeded(event);

            // then
            Order reloaded = orderService.getOrderById(order.getId());
            assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.PAID);
            verify(couponService, times(1)).useCoupon(user.getUserId(), userCoupon.getId());
        }

        @Test
        @DisplayName("handlePaymentFailed: 주문을 FAILED로 변경하고 각 아이템 재고 원복")
        void handlePaymentFailed_updates_order_and_restores_stock() {
            // given
            String userId = "oyyFailed";
            User user = setupUser(userId);
            Brand brand = brandRepository.save(TestFixture.createBrand());
            Product product = productRepository.save(TestFixture.createProduct(brand));
            List<OrderItem> orderItems = TestFixture.createOrderItems(product, 1);
            Order order = orderService.createOrder(user, orderItems, 4000, null);

            assertThat(order.getStatus()).isNotEqualTo(OrderStatus.FAILED);

            PaymentFailedEvent event = new PaymentFailedEvent(
                    order.getId(), user.getUserId(), 8888L
            );

            // when
            paymentFacade.handlePaymentFailed(event);

            // then
            Order reloaded = orderService.getOrderById(order.getId());
            assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.FAILED);
            verify(productService, times(1)).restoreStock(product.getId(), 1);
            verify(couponService, never()).useCoupon(anyString(), anyLong());
        }

    }
}
