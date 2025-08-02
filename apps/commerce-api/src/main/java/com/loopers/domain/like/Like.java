package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Optional;

@Entity
@Getter
@Table(name = "likes")
public class Like extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "liked_yn")
    private LikeStatus likedYn;

    public void like() {
        this.likedYn = LikeStatus.Y;
    }

    public void unLike() {
        this.likedYn = LikeStatus.N;
    }

    public static Like likeOrCreate(Optional<Like> maybeLike, User user, Product product) {
        if (maybeLike.isPresent()) {
            Like existingLike = maybeLike.get();
            existingLike.like();
            return existingLike;
        }
        return Like.create(user, product);
    }

    public static Like create(User user, Product product) {
        Like like = new Like();
        like.user = user;
        like.product = product;
        like.likedYn = LikeStatus.Y;
        return like;
    }

    public boolean isLiked() {
        return this.likedYn.isLiked();
    }

}
