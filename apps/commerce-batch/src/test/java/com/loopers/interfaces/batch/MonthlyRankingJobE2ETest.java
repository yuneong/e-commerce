package com.loopers.interfaces.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@SpringBatchTest
@AutoConfigureMockMvc
class MonthlyRankingJobE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job monthlyRankingJob;

    @Test
    @DisplayName("성공: API 호출 시 실제 배치 잡이 실행된다")
    void runMonthlyRankingJob_success() throws Exception {
        // given
        jobLauncherTestUtils.setJob(monthlyRankingJob);

        // when: API 호출
        mockMvc.perform(post("/api/v1/batch/monthly-ranking")
                        .param("yearMonth", "2025-08"))
                .andExpect(status().isOk())
                .andExpect(content().string("monthlyRankingJob started!"));

        // then: 잡 실행 결과 검증
        JobParameters params = new JobParametersBuilder()
                .addString("yearMonth", "2025-08")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("실패: yearMonth 파라미터 누락 시 400 반환")
    void runMonthlyRankingJob_missingParam() throws Exception {
        mockMvc.perform(post("/api/v1/batch/monthly-ranking"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 잘못된 yearMonth 포맷이면 500 반환")
    void runMonthlyRankingJob_invalidYearMonth() throws Exception {
        mockMvc.perform(post("/api/v1/batch/monthly-ranking")
                        .param("yearMonth", "2025/08"))
                .andExpect(status().is5xxServerError());
    }

}
