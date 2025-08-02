package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.like.dto.LikeCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository likeJpaRepository;

    @Override
    public List<LikeCountDto> countGroupByProductIds(List<Long> productIds) {
        return likeJpaRepository.countGroupByProductIds(productIds);
    }

    @Override
    public Long countGroupByProductId(Long productId) {
        return likeJpaRepository.countGroupByProductId(productId);
    }

    @Override
    public Optional<Like> findByProductAndUser(Product product, User user) {
        return likeJpaRepository.findByProductAndUser(product, user);
    }

    @Override
    public Like save(Like like) {
        return likeJpaRepository.save(like);
    }

    @Override
    public List<Like> findByUserJoinProduct(User user) {
        return likeJpaRepository.findByUserJoinProduct(user);
    }

}
