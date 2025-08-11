package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Table(name = "likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Version
    private Long version;

    public void like() {
        this.likedYn = LikeStatus.Y;
    }

    public void unLike() {
        this.likedYn = LikeStatus.N;
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
