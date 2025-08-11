package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface PointJpaRepository extends JpaRepository<Point, Long> {

    Point findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Point p WHERE p.user = :user")
    Point findUserWithLock(@Param("user") User user);

}
