package com.loopers.application.product;


import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;

    public ProductListInfo getProducts(ProductCommand command, String userId) {
        // service
        Page<Product> products = productService.getProducts(command);

        // 추후 로그인시 likedYn 추가

        // domain -> result
        return ProductListInfo.from(products);
    }

    public ProductInfo getProductDetail(Long productId, String userId) {
        // service
        Product product = productService.getProductDetail(productId);

        // 추후 로그인시 likedYn 추가

        // domain -> result
        return ProductInfo.from(product);
    }

}
