package com.loopers.application.product;


import com.loopers.domain.common.UserActionEnvelope;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    public ProductListInfo getProducts(ProductCommand command, String userId) {
        // service
        Page<Product> products = productService.getProducts(command);

        // 추후 로그인시 likedYn 추가

        // event
        eventPublisher.publishEvent(UserActionEnvelope.of(
                "PRODUCT_VIEWED",
                ProductViewedEvent.of(products.getContent().stream().map(Product::getId).toList())
        ));

        // domain -> result
        return ProductListInfo.from(products);
    }

    public ProductInfo getProductDetail(Long productId, String userId) {
        // service
        Product product = productService.getProductDetail(productId);

        // 추후 로그인시 likedYn 추가

        // event
        eventPublisher.publishEvent(UserActionEnvelope.of(
                "PRODUCT_DETAIL_VIEWED",
                ProductViewedEvent.of(productId)
        ));

        // domain -> result
        return ProductInfo.from(product);
    }

}
