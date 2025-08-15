package com.loopers.domain.point;

import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    @Transactional
    public Point create(User user) {
        // create new Point
        Point point = Point.create(user);
        // repository
        return pointRepository.save(point);
    }

    @Transactional
    public Point charge(User user, Long amount) {
        // validation point exists for userId
        Point point = pointRepository.findByUser(user);
        if (point == null) {
            throw new IllegalArgumentException("Point not found for user: " + user.getUserId());
        }
        // command -> domain
        point.charge(amount);
        // repository
        return pointRepository.save(point);
    }


    public Point getPoint(User user) {
        // repository
        Point point = pointRepository.findByUser(user);
        if (point == null) {
            throw new IllegalArgumentException("Point not found for user: " + user.getUserId());
        }

        return point;
    }

    @Transactional
    public Point getPointWithLock(User user) {
        // repository
        return Optional.ofNullable(pointRepository.findUserWithLock(user))
                .orElseThrow(() -> new IllegalArgumentException("Point not found for user: " + user.getUserId()));
    }

    @Transactional
    public Point checkAndUsePoint(Point point, int totalPrice) {
        // 포인트 차감
        point.use(totalPrice);
        // 저장
        return pointRepository.save(point);
    }

}
