package com.loopers;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
class SampleJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job sampleJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(sampleJob);
    }

    @Test
    void sampleJob_실행_테스트() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("targetDate", "2024-09-05")
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(BatchStatus.COMPLETED).isEqualTo(execution.getStatus());
    }
}

