package com.loopers.interfaces.listener;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.event.PaymentSucceededEvent;
import com.loopers.infrastructure.kafka.producer.StockChangedEventProducer;
import com.loopers.infrastructure.platform.DataPlatformSender;
import com.loopers.infrastructure.platform.OrderResultMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentFacade paymentFacade;
    private final OrderService orderService;
    private final DataPlatformSender dataPlatformSender;
    private final StockChangedEventProducer stockChangedEventProducer;

    /**
     * 결제 성공 이벤트 처리
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePaymentSucceededEvent(PaymentSucceededEvent event) {
        paymentFacade.handlePaymentSucceeded(event);
        dataPlatformSender.sendOrderResult(
                new OrderResultMessage(
                        event.userId(),
                        event.orderId(),
                        event.paymentId()
                )
        );

        // kafka 집계 - 판매량
        Order order = orderService.getOrderById(event.orderId());
        List<OrderItem> orderItems = order.getOrderItems();

        for (OrderItem item : orderItems) {
            stockChangedEventProducer.sendStockChangedEvent(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    "SUCCESS"
            );
        }
    }

    /**
     * 결제 실패 이벤트 처리
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        paymentFacade.handlePaymentFailed(event);
        dataPlatformSender.sendOrderResult(
                new OrderResultMessage(
                        event.userId(),
                        event.orderId(),
                        event.paymentId()
                )
        );

        // kafka 집계 - 판매량
        Order order = orderService.getOrderById(event.orderId());
        List<OrderItem> orderItems = order.getOrderItems();

        for (OrderItem item : orderItems) {
            stockChangedEventProducer.sendStockChangedEvent(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    "FAIL"
            );
        }
    }

}
