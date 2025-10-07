package com.back2basics.config;

import com.back2basics.job.projectstatus.ProjectStatusUpdateProcessor;
import com.back2basics.project.model.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JpaPagingItemReader<Project> projectStatusUpdateItemReader;
    private final JpaItemWriter<Project> projectStatusUpdateItemWriter;

    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job projectStatusUpdateJob() {
        return new JobBuilder("projectStatusUpdateJob", jobRepository)
            .start(projectStatusUpdateStep())
            .build();
    }

    @Bean
    public Step projectStatusUpdateStep() {
        return new StepBuilder("projectStatusUpdateStep", jobRepository)
            .<Project, Project>chunk(CHUNK_SIZE, transactionManager)
            .reader(projectStatusUpdateItemReader)
            .processor(new ProjectStatusUpdateProcessor())
            .writer(projectStatusUpdateItemWriter)
            .build();
    }
}
