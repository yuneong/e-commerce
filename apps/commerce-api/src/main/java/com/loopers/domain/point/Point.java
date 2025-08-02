package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "points")
public class Point extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private long amount;
    private long balance; // 총 보유 잔액


    public static Point create(User user) {
        Point point = new Point();

        point.user = user;
        point.amount = 0L; // 초기 충전 금액
        point.balance = 0L; // 초기 잔액

        return point;
    }

    public Point charge(long amount) {
        validateAmount(amount, "charge");
        this.balance += amount; // 충전 시 잔액 증가

        return this;
    }

    public Point use(long amount) {
        validateAmount(amount, "use");
        if (this.balance < amount) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        this.balance -= amount;

        return this;
    }

    public void validateAmount(long amount, String type) {
        if (amount <= 0L) {
            throw new IllegalArgumentException(
                    String.format("%s 금액은 0보다 커야 합니다.", type.equals("charge") ? "충전" : "차감")
            );
        }
    }

}
