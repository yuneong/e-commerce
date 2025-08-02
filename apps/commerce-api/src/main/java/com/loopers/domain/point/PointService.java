package com.loopers.domain.point;

import com.loopers.application.point.PointCommand;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    public Point create(String userId) {
        // user
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with userId: " + userId));
        // create new Point
        Point point = Point.create(user);
        // repository
        return pointRepository.save(point);
    }

    @Transactional
    public Point charge(PointCommand command) {
        // validation point exists for userId
        User user = userRepository.findByUserId(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with userId: " + command.userId()));
        Point point = pointRepository.findByUser(user);

        // command -> domain
        point.charge(command.amount());
        // repository
        return pointRepository.save(point);
    }

    public Point getPoint(User user) {
        // repository
        return pointRepository.findByUser(user);
    }

}
