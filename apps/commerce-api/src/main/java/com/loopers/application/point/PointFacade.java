package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final PointService pointService;
    private final UserService userService;

    public PointInfo charge(PointCommand command) {
        // validation user exists
        User user = userService.getMyInfo(command.userId());
        // service
        Point point = pointService.charge(user, command.amount());
        // domain -> result
        return PointInfo.from(point);
    }

    @Transactional(readOnly = true)
    public PointInfo getPoint(String userId) {
        // service
        User user = userService.getMyInfo(userId);
        Point point = pointService.getPoint(user);

        // domain -> result
        return PointInfo.from(point);
    }

}
