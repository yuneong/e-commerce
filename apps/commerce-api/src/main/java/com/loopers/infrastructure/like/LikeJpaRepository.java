package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.like.dto.LikeCountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface LikeJpaRepository extends JpaRepository<Like, Long> {

    @Query("""
            SELECT new com.loopers.infrastructure.like.dto.LikeCountDto(l.product.id, COUNT(l.id))
            FROM Like l
            WHERE l.product.id IN :productIds
                AND l.likedYn = 'Y'
            GROUP BY l.product.id
        """)
    List<LikeCountDto> countGroupByProductIds(List<Long> productIds);

    @Query("""
            SELECT COUNT(l.id)
            FROM Like l
            WHERE l.product.id = :productId
                AND l.likedYn = 'Y'
            GROUP BY l.product.id
        """)
    Long countGroupByProductId(Long productId);

    Optional<Like> findByProductAndUser(Product product, User user);

    @Query("""
            SELECT l
            FROM Like l
            JOIN FETCH l.product p
            WHERE l.user = :user
                AND l.likedYn = 'Y'
        """)
    List<Like> findByUserJoinProduct(User user);

}
