package com.loopers.domain.point;

import com.loopers.domain.user.User;

public interface PointRepository {

    Point save(Point point);

    Point findByUser(User user);

    Point findUserWithLock(User user);

}
