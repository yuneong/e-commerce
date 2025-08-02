package com.loopers.application.product;


import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final LikeService likeService;

    public ProductListInfo getProducts(ProductCommand command, String userId) {
        // service
        Page<Product> products = productService.getProducts(command);

        List<Long> productIds = products.stream()
                .map(Product::getId)
                .toList();

        // likeCount
        Map<Long, Long> likeCounts = likeService.getLikeCounts(productIds);

        // 추후 여유가 생기면 구현 -> 로그인시 likedYn 추가

        // product, likeCount mapping VO
        ProductContentBundle bundle = ProductContentBundle.create(products, likeCounts);

        // domain -> result
        return ProductListInfo.from(bundle, products.getPageable(), products.getTotalElements());
    }

    public ProductInfo getProductDetail(Long productId, String userId) {
        // service
        Product product = productService.getProductDetail(productId);

        // likeCount
        Long likeCount = likeService.getLikeCount(productId);

        // 추후 여유가 생기면 구현 -> 로그인시 likedYn 추가

        // domain -> result
        return ProductInfo.from(product, likeCount);
    }

}
