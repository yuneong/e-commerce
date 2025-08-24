package com.loopers.application.like;


import com.loopers.domain.like.LikeStatus;

public record LikeInfo(
        LikeStatus likedYn
) {

    public static LikeInfo of(LikeStatus likedYn) {
        return new LikeInfo(
                likedYn
        );
    }

}
