package com.loopers.domain.like;

public enum LikeStatus {
    Y, N;

    public boolean isLiked() {
        return this == Y;
    }

}
