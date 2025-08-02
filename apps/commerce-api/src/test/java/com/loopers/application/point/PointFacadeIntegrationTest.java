package com.loopers.application.point;

import com.loopers.application.user.UserCommand;
import com.loopers.application.user.UserFacade;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PointFacadeIntegrationTest {

    @Autowired
    private PointFacade pointFacade;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
    }

    /**
     * - [x]  존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.
     */
    @DisplayName("포인트 충전 시,")
    @Nested
    class pointCharge {

        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void failToCharge_whenUserIdDoesNotExist() {
            // given
            PointCommand command = new PointCommand("oyy", 500L);

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> {
                pointFacade.charge(command);
            });

            assertAll(
                    () -> assertThat(exception.getMessage()).isEqualTo("존재하지 않는 유저 ID 입니다."),
                    () -> assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND)
            );
        }

    }

    /**
     * - [x]  해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.
     * - [x]  해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.
     */
    @DisplayName("포인트 조회 시,")
    @Nested
    class getPoint {

        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        @Transactional
        void returnsBalance_whenUserExistsByUserId() {
            // given
            String userId = "oyy";
            UserCommand command = new UserCommand(
                    userId,
                    Gender.F,
                    "1999-08-21",
                    "loopers@gmail.com"
            );
            userFacade.signUp(command);

            // when
            PointInfo pointInfo = pointFacade.getPoint(userId);

            // then
            assertAll(
                    () -> assertThat(pointInfo).isNotNull(),
                    () -> assertThat(pointInfo.userId()).isEqualTo(userId),
                    () -> assertThat(pointInfo.balance()).isGreaterThanOrEqualTo(0L)
            );
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnsNull_whenUserDoesNotExist() {
            // given
            String userId = "oyy";

            // when
            PointInfo pointInfo = pointFacade.getPoint(userId);

            // then
            assertThat(pointInfo).isNull();
            assertThat(pointInfo).isEqualTo(null);
        }
    }

}
