package com.loopers.interfaces.api.product;

import com.loopers.domain.product.ProductLikeEvent;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 10, multiplier = 2.0, maxDelay = 200)
    )
    public void handle(ProductLikeEvent event) {
//        System.out.println("@@@ ProductLikeEventListener.handle - event: " + event);
        productService.updateLikeCount(event.productId(), event.likeType());
    }

}
