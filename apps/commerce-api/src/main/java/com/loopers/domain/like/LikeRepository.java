package com.loopers.domain.like;


import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.like.dto.LikeCountDto;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {

    List<LikeCountDto> countGroupByProductIds(List<Long> productIds);

    Long countGroupByProductId(Long productId);

    Optional<Like> findByProductAndUser(Product product, User user);

    Like save(Like like);

    List<Like> findByUserJoinProduct(User user);

}
