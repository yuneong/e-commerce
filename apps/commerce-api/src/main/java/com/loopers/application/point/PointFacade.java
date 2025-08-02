package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final PointService pointService;
    private final UserService userService;

    @Transactional
    public PointInfo charge(PointCommand command) {
        // validation user exists
        if (!userService.existsByUserId(command.userId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 유저 ID 입니다.");
        }

        // service
        Point point = pointService.charge(command);
        // domain -> result
        return PointInfo.from(point);
    }

    @Transactional(readOnly = true)
    public PointInfo getPoint(String userId) {
        // service
        User user = userService.getMyInfo(userId);
        Point point = pointService.getPoint(user);
        if (point == null) {
            return null;
        }
        // domain -> result
        return PointInfo.from(point);
    }

}
