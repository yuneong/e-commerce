package com.loopers.interfaces.listener;

import com.loopers.domain.product.event.ProductLikeEvent;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductLikeEventListener {

    private final ProductService productService;

    /**
     * 좋아요 집계 이벤트 처리
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductLikeCountEvent(ProductLikeEvent event) {
        productService.updateLikeCount(event.productId(), event.likeType());
    }

}
