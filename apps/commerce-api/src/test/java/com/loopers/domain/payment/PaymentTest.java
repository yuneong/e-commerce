package com.loopers.domain.payment;

import com.loopers.application.payment.PaymentMethod;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private CardDetail validCard() {
        return new CardDetail("1234-5678-1234-5678", CardType.SAMSUNG, null);
    }

    private Payment newValidPointPayment() {
        return Payment.createPointPayment("user1", 100L, 1000);
    }

    private Payment newValidCardPayment() {
        return Payment.createCardPayment("user1", 100L, 1000, validCard());
    }


    @DisplayName("포인트 결제 생성")
    @Nested
    class CreatePointPayment {

        @DisplayName("성공 - 유효한 userId, orderId, amount")
        @Test
        void success_withValidData() {
            Payment pointPayment = Payment.createPointPayment("user1", 100L, 1000);

            assertThat(pointPayment.getMethod()).isEqualTo(PaymentMethod.POINT);
            assertThat(pointPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(pointPayment.getAmount()).isEqualTo(1000);
            assertThat(pointPayment.getCardDetail()).isNull();
        }

        @DisplayName("실패 - userId가 공백")
        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t"})
        void fail_whenUserIdBlank(String userId) {
            assertThatThrownBy(() -> Payment.createPointPayment(userId, 1L, 100))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("유저 ID는 필수값입니다.");
        }

        @DisplayName("실패 - orderId가 양수가 아님")
        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -10L})
        void fail_whenOrderIdNonPositive(long orderId) {
            assertThatThrownBy(() -> Payment.createPointPayment("user1", orderId, 100))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("주문 ID는 양수여야 합니다.");
        }

        @DisplayName("실패 - amount가 양수가 아님")
        @ParameterizedTest
        @ValueSource(ints = {-1, -100})
        void fail_whenAmountNonPositive(int amount) {
            assertThatThrownBy(() -> Payment.createPointPayment("user1", 1L, amount))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("결제금액은 0 이상이어야 합니다.");
        }

    }


    @DisplayName("카드 결제 생성")
    @Nested
    class CreateCardPayment {

        @DisplayName("성공 - 유효한 카드 정보")
        @ParameterizedTest
        @ValueSource(strings = {"1234-5678-1234-5678", "4321-8765-4321-8765"})
        void createCardPayment_success_withValidCard(String cardNo) {
            CardDetail cardDetail = new CardDetail(cardNo, CardType.SAMSUNG, null);
            Payment cardPayment = Payment.createCardPayment("user1", 1L, 1000, cardDetail);

            assertThat(cardPayment.getMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(cardPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(cardPayment.getAmount()).isEqualTo(1000);
            assertThat(cardPayment.getCardDetail().getCardNo()).isEqualTo(cardNo);
        }

        @DisplayName("실패 - 카드번호 포맷이 유효하지 않음")
        @ParameterizedTest
        @ValueSource(strings = {
                "", " ", "\t",
                "1234567812345678",           // 하이픈 없음
                "1234-5678-1234-567",         // 자리수 부족
                "1234-5678-1234-56789",       // 자리수 초과
                "1234-5678-1234-ABCD"         // 숫자 아님
        })
        void createCardPayment_fail_whenCardNoInvalid(String badCardNo) {
            CardDetail invalid = new CardDetail(badCardNo, CardType.SAMSUNG, null);

            assertThatThrownBy(() -> Payment.createCardPayment("user1", 1L, 1000, invalid))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("카드 번호는 xxxx-xxxx-xxxx-xxxx 형식이어야 합니다.");
        }

        @DisplayName("실패 - userId가 공백")
        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t"})
        void createCardPayment_fail_whenUserIdBlank(String userId) {
            assertThatThrownBy(() -> Payment.createCardPayment(userId, 1L, 1000, validCard()))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("유저 ID는 필수값입니다.");
        }

        @DisplayName("실패 - orderId가 양수가 아님")
        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -99L})
        void createCardPayment_fail_whenOrderIdNonPositive(long orderId) {
            assertThatThrownBy(() -> Payment.createCardPayment("user1", orderId, 1000, validCard()))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("주문 ID는 양수여야 합니다.");
        }

        @DisplayName("실패 - amount가 양수가 아님")
        @ParameterizedTest
        @ValueSource(ints = {-1, -100})
        void createCardPayment_fail_whenAmountNonPositive(int amount) {
            assertThatThrownBy(() -> Payment.createCardPayment("user1", 1L, amount, validCard()))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("결제금액은 0 이상이어야 합니다.");
        }

    }


    @DisplayName("카드 결제 상태 업데이트")
    @Nested
    class UpdateStatus {

        @DisplayName("성공 - 유효한 상태와 이유")
        @ParameterizedTest
        @ValueSource(strings = {"SUCCESS", "FAILED", "PENDING"})
        void success_withValidStatus(String status) {
            Payment cardPayment = newValidCardPayment();
            cardPayment.updateStatus(PaymentStatus.valueOf(status), "어쩌고 이유~~");

            assertThat(cardPayment.getStatus()).isEqualTo(PaymentStatus.valueOf(status));
            assertThat(cardPayment.getReason()).isEqualTo("어쩌고 이유~~");
        }

        @DisplayName("실패 - 상태가 null")
        @Test
        void fail_whenStatusNull() {
            Payment cardPayment = newValidCardPayment();

            assertThatThrownBy(() -> cardPayment.updateStatus(null, "어쩌고 이유~~"))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("결제 상태는 필수값입니다.");
        }

    }


    @DisplayName("카드 결제 트랜잭션 키 업데이트")
    @Nested
    class UpdateTransactionKey {

        @DisplayName("성공 - 유효한 거래 키")
        @ParameterizedTest
        @ValueSource(strings = {"T1", "T2", "TX-1234567890"})
        void success_withValidKey(String validKey) {
            Payment cardPayment = newValidCardPayment();
            cardPayment.updateTransactionKey(validKey);

            assertThat(cardPayment.getCardDetail().getTransactionKey()).isEqualTo(validKey);
        }

        @DisplayName("실패 - 거래 키 공백")
        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t"})
        void fail_whenBlankKey(String blankKey) {
            Payment cardPayment = newValidCardPayment();

            assertThatThrownBy(() -> cardPayment.updateTransactionKey(blankKey))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("거래 키는 공백일 수 없습니다.");
        }

        @DisplayName("실패 - 카드 결제가 아님")
        @ParameterizedTest
        @ValueSource(strings = {"T1", "T2"})
        void fail_whenNotCardPayment(String anyKey) {
            Payment cardPayment = newValidPointPayment();

            assertThatThrownBy(() -> cardPayment.updateTransactionKey(anyKey))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("거래 키는 카드 결제에만 적용 가능합니다.");
        }

    }



}
