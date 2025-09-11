package com.loopers.application.product;


import com.loopers.domain.common.UserActionEnvelope;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;

    public static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

    public String buildKey(LocalDate date) {
        return "ranking:all:" + date.format(DAY);
    }

    public String todayKey() {
        return buildKey(LocalDate.now(ZoneId.of("Asia/Seoul")));
    }


    public ProductListInfo getProducts(ProductCommand command, String userId) {
        // service
        Page<Product> products = productService.getProducts(command);

        // 추후 로그인시 likedYn 추가

        // event
        eventPublisher.publishEvent(UserActionEnvelope.of(
                "PRODUCT_VIEWED",
                userId,
                ProductViewedEvent.of(products.getContent().stream().map(Product::getId).toList())
        ));

        // domain -> result
        return ProductListInfo.from(products);
    }

    public ProductInfo getProductDetail(Long productId, String userId) {
        // service
        Product product = productService.getProductDetailForCaching(productId);

        // 랭킹 (존재하지 않으면 null)
        Long rawRank = redisTemplate.opsForZSet().reverseRank(todayKey(), productId.toString());
        Long rank = (rawRank != null) ? rawRank + 1 : null;

        // 추후 로그인시 likedYn 추가

        // event
        eventPublisher.publishEvent(UserActionEnvelope.of(
                "PRODUCT_DETAIL_VIEWED",
                userId,
                ProductViewedEvent.of(productId)
        ));

        // domain -> result
        return ProductInfo.from(product, rank);
    }

}
