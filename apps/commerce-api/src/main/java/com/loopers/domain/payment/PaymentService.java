package com.loopers.domain.payment;

import com.loopers.application.payment.CallbackPaymentCommand;
import com.loopers.application.payment.ProcessPaymentCommand;
import com.loopers.infrastructure.pg.PgService;
import com.loopers.infrastructure.pg.PgV1Dto;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PgService pgService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Payment createPayment(ProcessPaymentCommand command) {
        Payment payment = switch (command.paymentMethod()) {
            case CARD -> Payment.createCardPayment(
                    command.userId(),
                    command.orderId(),
                    command.amount(),
                    new CardDetail(
                            command.cardNo(),
                            command.cardType(),
                            null // 최초 생성 시 transactionKey 없음
                    )
                );
            case POINT -> Payment.createPointPayment(
                        command.userId(),
                        command.orderId(),
                        command.amount()
                );
        };

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment requestAndSavePayment(Payment payment, String callbackUrl) {
        PgV1Dto.PgRequest request = PgV1Dto.PgRequest.from(payment, callbackUrl);

        try {
            ApiResponse<PgV1Dto.PgResponse> response = pgService.callPayment(payment.getUserId(), request);
            payment.updateTransactionKey(response.data().transactionKey());

            return paymentRepository.save(payment);
        } catch (Exception e) {
            // 실패 정책: 결제 FAILED + 주문 롤백 이벤트 발행
            payment.updateStatus(PaymentStatus.FAILED, e.getMessage());
            eventPublisher.publishEvent(new PaymentFailedEvent(payment.getOrderId(), payment.getUserId()));

            return paymentRepository.save(payment);
        }
    }

    @Transactional
    public void updateStatusFromCallback(CallbackPaymentCommand command) {
        Payment payment = paymentRepository.findByTransactionKeyAndOrderId(command.transactionKey(), command.orderId());

        payment.updateStatus(PaymentStatus.valueOf(command.status()), command.reason());

        paymentRepository.save(payment);
    }

    @Transactional
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Transactional
    public void updatePaymentStatusWithScheduler() {
        paymentRepository.findByStatus(PaymentStatus.PENDING)
                .forEach(payment -> {
                    ApiResponse<PgV1Dto.PgDetailResponse> response = pgService.getPaymentDetail(payment);

                    if (response.data().status() != PaymentStatus.PENDING) { // 폴백이면 PENDING 유지
                        payment.updateStatus(response.data().status(), response.data().reason());
                        paymentRepository.save(payment);
                    }
                });
    }

}
