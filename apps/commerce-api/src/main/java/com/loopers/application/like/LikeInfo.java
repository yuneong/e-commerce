package com.loopers.application.like;


import com.loopers.domain.like.LikeStatus;

public record LikeInfo(
        LikeStatus likedYn,
        Long likeCount
) {

    public static LikeInfo of(LikeStatus likedYn, Long likeCount) {
        return new LikeInfo(
                likedYn,
                likeCount
        );
    }

}
