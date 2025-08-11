package com.loopers.domain.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.like.dto.LikeCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    public Map<Long, Long> getLikeCounts(List<Long> productIds) {
        Map<Long, Long> likeCountMap = likeRepository.countGroupByProductIds(productIds).stream()
                .collect(Collectors.toMap(
                        LikeCountDto::getProductId,
                        LikeCountDto::getCount
                ));

        for (Long id : productIds) {
            // 좋아요가 0건인 상품은 likeCountMap 에 포함되지 않으므로
            // 상품 ID가 없으면 0으로 초기화하여 추가함
            likeCountMap.putIfAbsent(id, 0L);
        }

        return likeCountMap;
    }

    public Long getLikeCount(Long productId) {
        return Optional.ofNullable(likeRepository.countGroupByProductId(productId))
                .orElse(0L);
    }

    @Transactional
    public Like like(Product product, User user) {
        return likeRepository.findByProductAndUser(product, user)
                .map(existingLike -> {
                    existingLike.like();
                    return existingLike;
                })
                .orElse(Like.create(user, product));
    }

    @Transactional
    public Like unLike(Product product, User user) {
        Like like = likeRepository.findByProductAndUser(product, user)
                .orElseThrow(() -> new IllegalArgumentException("Like not found for product and user"));

        like.unLike();

        return like;
    }

    public List<Like> getLikeProducts(User user) {
        return likeRepository.findByUserJoinProduct(user);
    }

}
