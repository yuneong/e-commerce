package com.loopers.interfaces.listener;

import com.loopers.domain.order.event.OrderFailedEvent;
import com.loopers.domain.order.event.OrderSucceededEvent;
import com.loopers.infrastructure.platform.DataPlatformSender;
import com.loopers.infrastructure.platform.OrderResultMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final DataPlatformSender dataPlatformSender;

    /**
     * 주문 성공 이벤트 처리
     * 데이터 플랫폼 전송은 트랜잭션 커밋 후에 비동기로 처리
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderSucceededEvent(OrderSucceededEvent event) {
        System.out.println("!!!!success!!!!!!!handleOrderSucceededEvent called");
        dataPlatformSender.sendOrderResult(
                new OrderResultMessage(
                        event.userId(),
                        event.orderId()
                )
        );
    }

//    /**
//     * 주문 실패 이벤트 처리
//     * 데이터 플랫폼 전송을 비동기로 처리
//     * @param event
//     */
//    @EventListener
//    @Async
//    public void handleOrderFailedEvent(OrderFailedEvent event) {
//        System.out.println("!!!!!fail!!!!!!handleOrderFailedEvent called");
//        dataPlatformSender.sendOrderResult(
//                new OrderResultMessage(
//                        event.userId(),
//                        event.orderId(),
//                        event.status().toString()
//                )
//        );
//    }

}
