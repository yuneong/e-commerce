package com.loopers.application.like;


import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Component
public class LikeFacade {

    private final LikeService likeService;
    private final ProductService productService;
    private final UserService userService;

    @Transactional
    public LikeInfo like(Long productId, String userId) {
        // 상품, 유저 객체 조회
        Product product = productService.getProductDetail(productId);
        User user = userService.getMyInfo(userId); // 유저가 존재하지 않으면 예외 처리 필요 (현재는 통합 테스트 요구사항 때문에 Null로 처리 되어있음)

        // service
        Like savedLike = likeService.like(product, user);
        Long likeCount = likeService.getLikeCount(productId);

        // info
        return LikeInfo.of(savedLike.getLikedYn(), likeCount);
    }

    @Transactional
    public LikeInfo unLike(Long productId, String userId) {
        // 상품, 유저 객체 조회
        Product product = productService.getProductDetail(productId);
        User user = userService.getMyInfo(userId); // 유저가 존재하지 않으면 예외 처리 필요 (현재는 통합 테스트 요구사항 때문에 Null로 처리 되어있음)

        // service
        Like savedLike = likeService.unLike(product, user);
        Long likeCount = likeService.getLikeCount(productId);

        // info
        return LikeInfo.of(savedLike.getLikedYn(), likeCount);
    }

    public LikeListInfo getLikeProducts(String userId) {
        // 유저 객체 조회
        User user = userService.getMyInfo(userId); // 유저가 존재하지 않으면 예외 처리 필요 (현재는 통합 테스트 요구사항 때문에 Null로 처리 되어있음)

        // service
        List<Like> likeProducts = likeService.getLikeProducts(user);

        List<Long> productIds = likeProducts.stream()
                .map(like -> like.getProduct().getId())
                .toList();

        Map<Long, Long> likeCounts = likeService.getLikeCounts(productIds);

        return LikeListInfo.from(likeProducts, likeCounts);
    }

}
