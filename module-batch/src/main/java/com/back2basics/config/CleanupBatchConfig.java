package com.back2basics.config;

import com.back2basics.SoftDeletableCleaner;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class CleanupBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JPAQueryFactory jpaQueryFactory;
    private final List<SoftDeletableCleaner> cleaners;

    private static final int CHUNK_SIZE = 1000;

    @Bean
    public Job cleanupJob() {
        JobBuilder jobBuilder = new JobBuilder("cleanupSoftDeletedJob", jobRepository);
        Step[] steps = cleaners.stream()
            .map(this::buildCleanupStepForCleaner)
            .toArray(Step[]::new);

        return jobBuilder
            .start(steps[0])
            .next(steps)
            .build();
    }

    private Step buildCleanupStepForCleaner(SoftDeletableCleaner cleaner) {
        return new StepBuilder("cleanupStep_" + cleaner.getName(), jobRepository)
            .<Long, Long>chunk(CHUNK_SIZE, transactionManager)
            .reader(cleanupItemReader(cleaner))
            .writer(cleanupItemWriter(cleaner))
            .build();
    }

    private QueryDslNoOffsetItemReader<Long> cleanupItemReader(SoftDeletableCleaner cleaner) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        return new QueryDslNoOffsetItemReader<>(
            jpaQueryFactory,
            CHUNK_SIZE,
            cleaner.getIdPath(),
            cleaner.getIdExtractor(),
            cleaner.getQueryFunction(threshold)
        );
    }

    private ItemWriter<Long> cleanupItemWriter(SoftDeletableCleaner cleaner) {
        return items -> cleaner.bulkDelete(items);
    }
}
