package com.loopers.domain.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LikeTest {

    @DisplayName("Like 생성 시 likedYn은 Y 상태이다.")
    @Test
    void createLike() {
        // given
        User user = new User();
        Product product = new Product();

        // when
        Like like = Like.create(user, product);

        // then
        assertThat(like.getUser()).isEqualTo(user);
        assertThat(like.getProduct()).isEqualTo(product);
        assertThat(like.getLikedYn()).isEqualTo(LikeStatus.Y);
    }

    @DisplayName("like() 호출 시 상태가 Y로 변경된다.")
    @Test
    void like_shouldSetStatusToY() {
        // given
        Like like = new Like();
        like.unLike();

        // when
        like.like();

        // then
        assertThat(like.isLiked()).isTrue();
        assertThat(like.getLikedYn()).isEqualTo(LikeStatus.Y);
    }

    @DisplayName("unLike() 호출 시 상태가 N으로 변경된다.")
    @Test
    void unLike_shouldSetStatusToN() {
        // given
        Like like = new Like();
        like.like();

        // when
        like.unLike();

        // then
        assertThat(like.isLiked()).isFalse();
        assertThat(like.getLikedYn()).isEqualTo(LikeStatus.N);
    }

    @DisplayName("likeOrCreate - 기존 Like가 있으면 상태만 Y로 바꾼다.")
    @Test
    void likeOrCreate_whenExistingLike() {
        // given
        Like existing = new Like();
        existing.unLike();

        // when
        Like result = Like.likeOrCreate(Optional.of(existing), null, null);

        // then
        assertThat(result).isSameAs(existing); // 기존 객체와 동일한지 확인
        assertThat(result.isLiked()).isTrue();
        assertThat(result.getLikedYn()).isEqualTo(LikeStatus.Y);
    }

    @DisplayName("likeOrCreate - 기존 Like가 없으면 새 Like를 생성한다.")
    @Test
    void likeOrCreate_whenNoLike() {
        // given
        User user = new User();
        Product product = new Product();

        // when
        Like result = Like.likeOrCreate(Optional.empty(), user, product);

        // then
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getProduct()).isEqualTo(product);
        assertThat(result.isLiked()).isTrue();
        assertThat(result.getLikedYn()).isEqualTo(LikeStatus.Y);
    }

    @DisplayName("like()가 여러 번 호출되어도 상태는 Y로 유지된다.")
    @Test
    void like_isIdempotent() {
        // given
        Like like = new Like();
        like.like();

        // when
        like.like();
        like.like();

        // then
        assertThat(like.isLiked()).isTrue();
        assertThat(like.getLikedYn()).isEqualTo(LikeStatus.Y);
    }

    @DisplayName("unLike()가 여러 번 호출되어도 상태는 N로 유지된다.")
    @Test
    void unLike_isIdempotent() {
        // given
        Like like = new Like();
        like.unLike();

        // when
        like.unLike();
        like.unLike();

        // then
        assertThat(like.isLiked()).isFalse();
        assertThat(like.getLikedYn()).isEqualTo(LikeStatus.N);
    }


}
