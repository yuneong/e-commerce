package com.loopers.domain.payment;

import com.loopers.application.payment.CallbackPaymentCommand;
import com.loopers.application.payment.PaymentMethod;
import com.loopers.application.payment.ProcessPaymentCommand;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.*;
import com.loopers.domain.order.*;
import com.loopers.domain.order.Order;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.pg.PgClient;
import com.loopers.infrastructure.pg.PgV1Dto;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.TestFixture;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                // pgClient 오버라이드
                "spring.main.allow-bean-definition-overriding=true",

                // PG 결제 요청 설정
                // Retry: (기본 1 + 재시도 2) 총 3회 시도
                "resilience4j.retry.instances.pgRequest.max-attempts=3",
                "resilience4j.retry.instances.pgRequest.wait-duration=10ms",
                "resilience4j.retry.instances.pgRequest.retry-exceptions[0]=java.lang.RuntimeException",
                "resilience4j.retry.instances.pgRequest.retry-exceptions[1]=org.springframework.web.client.ResourceAccessException",
                "resilience4j.retry.instances.pgRequest.retry-exceptions[2]=org.springframework.web.client.HttpServerErrorException",
                // CircuitBreaker: 작은 창/낮은 임계치로 빠른 상태 전이
                "resilience4j.circuitbreaker.instances.pgRequest.sliding-window-type=COUNT_BASED",
                "resilience4j.circuitbreaker.instances.pgRequest.sliding-window-size=10",
                "resilience4j.circuitbreaker.instances.pgRequest.minimum-number-of-calls=10",
                "resilience4j.circuitbreaker.instances.pgRequest.failure-rate-threshold=50",
                "resilience4j.circuitbreaker.instances.pgRequest.wait-duration-in-open-state=1s",
                "resilience4j.circuitbreaker.instances.pgRequest.permitted-number-of-calls-in-half-open-state=3",

                // PG 결제 정보 조회 설정
                // Retry: 3회 시도
                "resilience4j.retry.instances.pgDetail.max-attempts=3",
                "resilience4j.retry.instances.pgDetail.wait-duration=10ms",
                "resilience4j.retry.instances.pgDetail.retry-exceptions[0]=java.lang.RuntimeException",
                // CircuitBreaker:
                "resilience4j.circuitbreaker.instances.pgDetail.sliding-window-type=COUNT_BASED",
                "resilience4j.circuitbreaker.instances.pgDetail.sliding-window-size=10",
                "resilience4j.circuitbreaker.instances.pgDetail.minimum-number-of-calls=10",
                "resilience4j.circuitbreaker.instances.pgDetail.failure-rate-threshold=100",
                "resilience4j.circuitbreaker.instances.pgDetail.wait-duration-in-open-state=1s",
                "resilience4j.circuitbreaker.instances.pgDetail.permitted-number-of-calls-in-half-open-state=3"
        }
)
class PaymentServiceIntegrationTest {

    @Autowired PaymentService paymentService;
    @Autowired PaymentRepository paymentRepository;
    @Autowired @Qualifier("testPgClient") TestPgClient pg; // 테스트 더블
    @Autowired TestEventListener eventListener;
    @Autowired private DatabaseCleanUp databaseCleanUp;
    private String callbackUrl = "http://localhost:8080/api/v1/payments/callback";
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private UserCouponRepository userCouponRepository;
    @Autowired private CouponService couponService;

    @TestConfiguration
    static class TestConfig {

        @Bean(name = {"testPgClient", "pgClient", "com.loopers.infrastructure.pg.PgClient"})
        @Primary
        TestPgClient testPgClient() {
            return new TestPgClient();
        }

        @Bean
        public TestEventListener eventListener() {
            return new TestEventListener();
        }
    }

    static class TestPgClient implements PgClient {
        private final AtomicInteger failsThenSuccess = new AtomicInteger(0); // n번 실패 후 성공(결제요청)
        private volatile boolean alwaysFail = false;                         // 항상 실패(결제요청)

        // 상세조회 실패 제어용
        private final AtomicInteger detailFailsThenSuccess = new AtomicInteger(0); // n번 실패 후 성공(상세조회)
        private volatile boolean detailAlwaysFail = false;                         // 항상 실패(상세조회)

        // 실제 호출 횟수
        private final AtomicInteger callCount = new AtomicInteger(0);
        private final AtomicInteger detailCallCount = new AtomicInteger(0);

        void setFailsThenSuccess(int n) { // 결제요청: n번 실패 후 성공
            failsThenSuccess.set(n);
            alwaysFail = false;
            callCount.set(0);
        }
        void setAlwaysFail(boolean v) {   // 결제요청: 항상 실패
            alwaysFail = v;
            callCount.set(0);
        }

        void setDetailFailsThenSuccess(int n) { // 상세조회: n번 실패 후 성공
            detailFailsThenSuccess.set(n);
            detailAlwaysFail = false;
            detailCallCount.set(0);
        }
        void setDetailAlwaysFail(boolean v) {   // 상세조회: 항상 실패
            detailAlwaysFail = v;
            detailCallCount.set(0);
        }

        void reset() { // 내부 상태 초기화
            setFailsThenSuccess(0);
            setAlwaysFail(false);
            callCount.set(0);

            detailFailsThenSuccess.set(0);
            detailAlwaysFail = false;
            detailCallCount.set(0);
        }

        int getCallCount() { return callCount.get(); }
        int getDetailCallCount() { return detailCallCount.get(); }

        @Override
        public ApiResponse<PgV1Dto.PgResponse> callPayment(String userId, PgV1Dto.PgRequest request) {
            callCount.incrementAndGet();

            if (alwaysFail) throw new RuntimeException("PG down");
            if (failsThenSuccess.getAndUpdate(x -> Math.max(0, x - 1)) > 0) {
                throw new RuntimeException("temporary failure");
            }

            return ApiResponse.success(new PgV1Dto.PgResponse(
                    "TX-" + UUID.randomUUID(),
                    "SUCCESS",
                    null
            ));
        }

        @Override
        public ApiResponse<PgV1Dto.PgDetailResponse> getPaymentDetail(String userId, String transactionKey) {
            detailCallCount.incrementAndGet();

            // 실패 시뮬레이션
            if (detailAlwaysFail) throw new RuntimeException("PG detail down");
            if (detailFailsThenSuccess.getAndUpdate(x -> Math.max(0, x - 1)) > 0) {
                throw new RuntimeException("temporary detail failure");
            }

            // 기본: 성공 응답
            return ApiResponse.success(new PgV1Dto.PgDetailResponse(
                    transactionKey,
                    1L, // 임의의 주문 ID
                    CardType.SAMSUNG,
                    "1234-5678-1234-5678",
                    1000,
                    PaymentStatus.SUCCESS,
                    null
            ));
        }
    }

    static class TestEventListener {
        private final List<PaymentFailedEvent> events = new CopyOnWriteArrayList<>(); // PaymentFailedEvent 이벤트 목록

        @org.springframework.context.event.EventListener
        public void on(PaymentFailedEvent e) { events.add(e); } // 이벤트 리스너 메서드

        public List<PaymentFailedEvent> get() { return events; } // 이벤트 목록 반환

        public void clear() { events.clear(); } // 이벤트 목록 초기화
    }

    private Payment newCardPending() {
        Payment cardPayment = Payment.createCardPayment(
                "oyy",
                1L,
                1000,
                new CardDetail(
                        "1234-5678-1234-5678",
                        CardType.SAMSUNG,
                        null
                )
        );
        return paymentRepository.save(cardPayment);
    }

    private Payment newPointPending() {
        Payment pointPayment = Payment.createPointPayment("user2", 200L, 500);

        return paymentRepository.save(pointPayment);
    }

    @BeforeEach
    void beforeEach() {
        eventListener.clear();
        pg.reset();

        // 데이터 세팅
        User user = TestFixture.createUser();
        User savedUser = userRepository.save(user);

        Brand brand = TestFixture.createBrand();
        Brand savedBrand = brandRepository.save(brand);

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

        Coupon coupon = couponRepository.save(new Coupon("10% 할인 쿠폰", CouponType.RATE, 10, 10, ZonedDateTime.now()));
        userCouponRepository.save(UserCoupon.create(savedUser.getUserId(), coupon.getId(), ZonedDateTime.now().plusDays(2)));
        DiscountedOrderByCoupon discountedOrderByCoupon = couponService.useCoupon(savedUser.getUserId(), coupon.getId(), items);

        orderService.createOrder(savedUser, items, discountedOrderByCoupon);
    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("createPayment()")
    @Nested
    class CreatePayment {

        @Test
        @DisplayName("성공 - 카드 결제 생성 및 저장")
        void success_card() {
            ProcessPaymentCommand cmd = new ProcessPaymentCommand(
                    "oyy",
                    1L,
                    1000,
                    PaymentMethod.CARD,
                    "1234-5678-1234-5678",
                    CardType.SAMSUNG
            );

            Payment saved = paymentService.createPayment(cmd);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(saved.getCardDetail()).isNotNull();
        }

        @Test
        @DisplayName("실패 - 카드번호 형식 위반")
        void fail_whenCardNoInvalid() {
            ProcessPaymentCommand cmd = new ProcessPaymentCommand(
                    "oyy",
                    1L,
                    1000,
                    PaymentMethod.CARD,
                    "1111222233334444",
                    CardType.SAMSUNG
            );

            assertThatThrownBy(() -> paymentService.createPayment(cmd))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("카드 번호는 xxxx-xxxx-xxxx-xxxx 형식이어야 합니다.");
        }

        @Test
        @DisplayName("성공 - 포인트 결제 생성 및 저장")
        void success_point() {
            ProcessPaymentCommand cmd = new ProcessPaymentCommand(
                    "user2",
                    200L,
                    500,
                    PaymentMethod.POINT,
                    null,
                    null
            );

            Payment saved = paymentService.createPayment(cmd);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getMethod()).isEqualTo(PaymentMethod.POINT);
            assertThat(saved.getCardDetail()).isNull();
        }

        @Test
        @DisplayName("실패 - 금액이 0 이하")
        void fail_whenAmountNonPositive() {
            ProcessPaymentCommand cmd = new ProcessPaymentCommand(
                    "user2",
                    200L,
                    -100,
                    PaymentMethod.POINT,
                    null,
                    null
            );

            assertThatThrownBy(() -> paymentService.createPayment(cmd))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("결제금액은 0 이상이어야 합니다.");
        }

    }


    @DisplayName("requestAndSavePayment()")
    @Nested
    class RequestAndSavePayment {

        @Test
        @DisplayName("성공 - PG 정상 응답 → 트랜잭션키 저장")
        void success_requestAndSavePayment() {
            Payment payment = newCardPending();

            Payment updated = paymentService.requestAndSavePayment(payment, callbackUrl);

            assertThat(updated.getCardDetail().getTransactionKey()).startsWith("TX-");
            Payment reloaded = paymentRepository.findById(updated.getId()).orElseThrow();
            assertThat(reloaded.getCardDetail().getTransactionKey())
                    .isEqualTo(updated.getCardDetail().getTransactionKey());
            assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(pg.getCallCount()).isEqualTo(1);
            assertThat(eventListener.get()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 2회 실패 후 성공(재시도 동작)")
        void success_whenRetry() {
            pg.setFailsThenSuccess(2);  // 앞 2회 실패 후 3번째 성공
            Payment payment = newCardPending();

            Payment updated = paymentService.requestAndSavePayment(payment, callbackUrl);

            assertThat(updated.getCardDetail().getTransactionKey()).startsWith("TX-");
            assertThat(pg.getCallCount()).isEqualTo(3); // 실패2 + 성공1

            Payment reloaded = paymentRepository.findById(updated.getId()).orElseThrow();
            assertThat(reloaded.getCardDetail().getTransactionKey())
                    .isEqualTo(updated.getCardDetail().getTransactionKey());

            assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(eventListener.get()).isEmpty();
        }

        @Test
        @DisplayName("실패 - 재시도 모두 실패 → FAILED 저장 & 이벤트 발행")
        void alwaysFail_thenFailedAndEvent() {
            pg.setAlwaysFail(true);
            Payment payment = newCardPending();

            Payment failed = paymentService.requestAndSavePayment(payment, callbackUrl);

            assertThat(pg.getCallCount()).isEqualTo(3);
            assertThat(failed.getStatus()).isEqualTo(PaymentStatus.FAILED);

            await().untilAsserted(() -> {
                Payment reloaded = paymentRepository.findById(failed.getId()).orElseThrow();
                assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.FAILED);

                // 이벤트 1건 발행 확인
                assertThat(eventListener.get()).hasSize(1);
                assertThat(eventListener.get().getFirst().orderId()).isEqualTo(payment.getOrderId());
                assertThat(eventListener.get().getFirst().userId()).isEqualTo(payment.getUserId());

                Order order = orderRepository.findById(reloaded.getOrderId()).orElseThrow();
                assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
            });
        }
    }


    @DisplayName("updateStatusFromCallback()")
    @Nested
    class UpdateStatusFromCallback {

        @Test
        @DisplayName("성공 - 콜백 상태로 업데이트(SUCCESS)")
        void success_updateStatusFromCallback() {
            Payment cardPending = newCardPending();
            cardPending.updateTransactionKey("TX-123"); // 트랜잭션 키 설정
            paymentRepository.save(cardPending);

            CallbackPaymentCommand cmd = new CallbackPaymentCommand(
                    "TX-123",
                    cardPending.getOrderId(),
                    "SAMSUNG",
                    "1234-5678-1234-5678",
                    1000,
                    "SUCCESS",
                    null
            );

            paymentService.updateStatusFromCallback(cmd);

            Payment reloaded = paymentRepository.findById(cardPending.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(reloaded.getReason()).isNull();
        }

        @Test
        @DisplayName("실패 - 미존재 키/주문ID (NPE 발생 가능)")
        void fail_WhenTransactionKeyIsNotFound() {
            CallbackPaymentCommand cmd = new CallbackPaymentCommand(
                    "NOPE",
                    999L,
                    "SAMSUNG",
                    "1234-5678-1234-5678",
                    1000,
                    "FAILED",
                    "Transaction not found"

            );

            assertThatThrownBy(() -> paymentService.updateStatusFromCallback(cmd))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("실패 - 잘못된 결제 상태 문자열")
        void fail_whenPaymentStatusIsInvalid() {
            Payment cardPending = newCardPending();
            cardPending.updateTransactionKey("TX-XYZ"); // 트랜잭션 키 설정
            paymentRepository.save(cardPending);

            CallbackPaymentCommand cmd = new CallbackPaymentCommand(
                    "TX-XYZ",
                    cardPending.getOrderId(),
                    "SAMSUNG",
                    "1234-5678-1234-5678",
                    1000,
                    "NOT_A_STATUS",
                    "PaymentStatus Invalid string"
            );

            // findByTransactionKeyAndOrderId 에서 null을 반환하지 않도록 미리 상태값 세팅
            cardPending.updateStatus(PaymentStatus.PENDING, null);
            paymentRepository.save(cardPending);

            assertThatThrownBy(() -> paymentService.updateStatusFromCallback(cmd))
                    .isInstanceOf(IllegalArgumentException.class);
        }

    }


    @DisplayName("savePayment()")
    @Nested
    class SavePayment {

        @Test
        @DisplayName("성공 - 결제 저장")
        void success_savePayment() {
            Payment pointPayment = Payment.createPointPayment("oyy", 1L, 10);

            Payment saved = paymentService.savePayment(pointPayment);

            assertThat(saved.getId()).isNotNull();
        }

    }


    @DisplayName("updatePaymentStatusWithScheduler()")
    @Nested
    class UpdatePaymentStatusWithScheduler {

        @Test
        @DisplayName("성공 - PENDING 동기화(SUCCESS로 변경)")
        void success_updatePaymentStatusWithScheduler() {
            Payment cardPending = newCardPending();
            cardPending.updateTransactionKey("TX-OK");
            cardPending.updateStatus(PaymentStatus.PENDING, null);
            paymentRepository.save(cardPending);

            paymentService.updatePaymentStatusWithScheduler();

            Payment reloaded = paymentRepository.findById(cardPending.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(pg.getDetailCallCount()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("성공 - 상세조회 2회 실패 후 성공(리트라이 동작) → SUCCESS로 변경")
        void success_detailRetryThenSuccess() {
            // given
            Payment cardPending = newCardPending();
            cardPending.updateTransactionKey("TX-RET");
            cardPending.updateStatus(PaymentStatus.PENDING, null);
            paymentRepository.save(cardPending);

            // pgClient: 앞 2회 실패 후 3번째 성공하도록 설정
            pg.setDetailFailsThenSuccess(2);

            // when
            paymentService.updatePaymentStatusWithScheduler();

            // then
            Payment reloaded = paymentRepository.findById(cardPending.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(pg.getDetailCallCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("실패 - 상세조회가 모두 실패해도 폴백(PENDING 유지)로 예외 없이 진행")
        void fallback_whenDetailAlwaysFail() {
            Payment cardPending = newCardPending();
            cardPending.updateTransactionKey("TX-RET");
            cardPending.updateStatus(PaymentStatus.PENDING, null);
            paymentRepository.save(cardPending);

            pg.setDetailAlwaysFail(true);

            // 폴백이 작동하므로 예외가 나면 안 됨
            paymentService.updatePaymentStatusWithScheduler();

            Payment reloaded = paymentRepository.findById(cardPending.getId()).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.PENDING); // 상태 유지
            assertThat(pg.getDetailCallCount()).isEqualTo(3);
        }

    }


}
