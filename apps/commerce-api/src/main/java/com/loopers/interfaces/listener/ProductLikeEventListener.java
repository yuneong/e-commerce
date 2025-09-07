package com.loopers.interfaces.listener;

import com.loopers.domain.product.event.ProductLikedEvent;
import com.loopers.domain.product.ProductService;
import com.loopers.infrastructure.kafka.producer.LikeChangedEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductLikeEventListener {

    private final ProductService productService;
    private final LikeChangedEventProducer likeChangedEventProducer;

    /**
     * 좋아요 집계 이벤트 처리
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductLikeCountEvent(ProductLikedEvent event) {
        // 내부 좋아요 집계
        productService.updateLikeCount(event.productId(), event.likeType());

        // kafka 집계 - 좋아요 수
        likeChangedEventProducer.sendLikeChangedEvent(event.productId(), event.likeType());
    }

}
