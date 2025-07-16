package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository pointJpaRepository;

    @Override
    public Point charge(Point point) {
        return pointJpaRepository.save(point);
    }

    @Override
    public Optional<Point> findByUserId(String userId) {
        return pointJpaRepository.findByUserId(userId);
    }

}
