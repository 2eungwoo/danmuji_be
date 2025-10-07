package com.back2basics;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyStatisticsBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job dailyStatisticsJob;

    @Scheduled(cron = "0 0 1 * * *")
    public void run() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();
        jobLauncher.run(dailyStatisticsJob, jobParameters);
    }
}
