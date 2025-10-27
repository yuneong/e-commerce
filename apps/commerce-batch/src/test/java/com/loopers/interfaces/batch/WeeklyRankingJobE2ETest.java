package com.loopers.interfaces.batch;

import com.loopers.domain.ranking.WeeklyRanking;
import com.loopers.domain.ranking.WeeklyRankingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@SpringBatchTest
@AutoConfigureMockMvc
class WeeklyRankingJobE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private WeeklyRankingRepository weeklyRankingRepository;

    @Test
    @DisplayName("성공: WeeklyRankingJob 실행 후 DB에 Top100 랭킹이 저장된다")
    void runWeeklyRankingJob_success() throws Exception {
        // given
        LocalDate startDate = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate endDate = LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY);

        mockMvc.perform(post("/api/v1/batch/weekly-ranking")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // then
        List<WeeklyRanking> rankings = weeklyRankingRepository.findByWeekStartAndWeekEnd(startDate, endDate);

        assertThat(rankings).isNotEmpty();
        assertThat(rankings).hasSizeLessThanOrEqualTo(100);

        assertThat(rankings).isSortedAccordingTo(Comparator.comparingInt(WeeklyRanking::getRank));

        assertThat(rankings.get(0).getScore()).isGreaterThan(0);
    }

    @Test
    @DisplayName("실패: 파라미터 누락 시 400 BadRequest")
    void runWeeklyRankingJob_fail_missingParams() throws Exception {
        mockMvc.perform(post("/api/v1/batch/weekly-ranking")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
