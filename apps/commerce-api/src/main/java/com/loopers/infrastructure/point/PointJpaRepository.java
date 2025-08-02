package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PointJpaRepository extends JpaRepository<Point, Long> {

    Point findByUser(User user);

}
