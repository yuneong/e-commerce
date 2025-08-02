package com.loopers.application.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.product.Product;

import java.util.List;
import java.util.Map;

public record LikeListInfo(
        List<LikeContent> contents
) {

    public static LikeListInfo from(List<Like> likes, Map<Long, Long> likeCounts) {
        List<LikeContent> likeContents = likes.stream()
                .map(like -> {
                    Product product = like.getProduct();
                    Long likeCount = likeCounts.getOrDefault(product.getId(), 0L);
                    return LikeContent.from(product, likeCount, like.getLikedYn());
                })
                .toList();

        return new LikeListInfo(likeContents);
    }

}
