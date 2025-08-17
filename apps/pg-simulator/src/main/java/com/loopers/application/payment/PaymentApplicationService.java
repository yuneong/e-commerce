package com.loopers.application.payment;

import com.loopers.domain.payment.*;
import com.loopers.domain.user.UserInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentApplicationService {

    private static final int RATE_LIMIT_EXCEEDED_START = 1;
    private static final int RATE_LIMIT_EXCEEDED_END = 20;
    private static final int RATE_INVALID_CARD_START = 21;
    private static final int RATE_INVALID_CARD_END = 30;

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentRelay paymentRelay;
    private final TransactionKeyGenerator transactionKeyGenerator;

    public PaymentApplicationService(
    PaymentRepository paymentRepository,
    PaymentEventPublisher paymentEventPublisher,
    PaymentRelay paymentRelay,
    TransactionKeyGenerator transactionKeyGenerator
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentEventPublisher = paymentEventPublisher;
        this.paymentRelay = paymentRelay;
        this.transactionKeyGenerator = transactionKeyGenerator;
    }

    @Transactional
    public TransactionInfo createTransaction(PaymentCommand command) {
        command.validate();

        String transactionKey = transactionKeyGenerator.generate();
        Payment payment = paymentRepository.save(
                Payment.create(
                        transactionKey,
                        command.userId(),
                        command.orderId(),
                        command.cardType(),
                        command.cardNo(),
                        command.amount(),
                        command.callBackUrl()
                )
        );

        paymentEventPublisher.publish(PaymentEvent.PaymentCreated.from(payment));
        return TransactionInfo.from(payment);
    }

    @Transactional(readOnly = true)
    public TransactionInfo getTransactionDetailInfo(UserInfo userInfo, String transactionKey) {
        Payment payment = paymentRepository.findByTransactionKey(userInfo.userId(), transactionKey)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "(transactionKey: " + transactionKey + ") 결제건이 존재하지 않습니다."));

        return TransactionInfo.from(payment);
    }

    @Transactional(readOnly = true)
    public OrderInfo findTransactionsByOrderId(UserInfo userInfo, Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(userInfo.userId(), orderId);
        if (payments.isEmpty()) {
            throw new CoreException(ErrorType.NOT_FOUND, "(orderId: " + orderId + ") 에 해당하는 결제건이 존재하지 않습니다.");
        }

        List<TransactionInfo> transactions = payments.stream()
            .map(TransactionInfo::from)
            .collect(Collectors.toList());

        return new OrderInfo(orderId, transactions);
    }

    @Transactional
    public void handle(String transactionKey) {
        Payment payment = paymentRepository.findByTransactionKey(transactionKey)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "(transactionKey: " + transactionKey + ") 결제건이 존재하지 않습니다."));

        int rate = (int) (Math.random() * 100) + 1;
        if (rate >= RATE_LIMIT_EXCEEDED_START && rate <= RATE_LIMIT_EXCEEDED_END) {
            payment.limitExceeded();
        } else if (rate >= RATE_INVALID_CARD_START && rate <= RATE_INVALID_CARD_END) {
            payment.invalidCard();
        } else {
            payment.approve();
        }
        paymentEventPublisher.publish(PaymentEvent.PaymentHandled.from(payment));
    }

    public void notifyTransactionResult(String transactionKey) {
        Payment payment = paymentRepository.findByTransactionKey(transactionKey)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "(transactionKey: " + transactionKey + ") 결제건이 존재하지 않습니다."));

        paymentRelay.notify(payment.getCallbackUrl(), TransactionInfo.from(payment));
    }
}
