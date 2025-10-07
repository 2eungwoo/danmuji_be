package com.back2basics.config;

import com.back2basics.adapter.persistence.statistics.entity.DailyStatisticsEntity;
import com.back2basics.adapter.persistence.statistics.repository.DailyStatisticsRepository;
import com.back2basics.global.config.CacheKeyProperties;
import com.back2basics.project.model.ProjectStatus;
import com.back2basics.project.model.StatusCountProjection;
import com.back2basics.project.port.out.ReadProjectPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ReadProjectPort readProjectPort;
    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final CacheKeyProperties cacheKeyProperties;

    @Bean
    public Job dailyStatisticsJob() {
        return new JobBuilder("dailyStatisticsJob", jobRepository)
            .start(dailyStatisticsStep())
            .build();
    }

    @Bean
    public Step dailyStatisticsStep() {
        return new StepBuilder("dailyStatisticsStep", jobRepository)
            .tasklet(dailyStatisticsTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet dailyStatisticsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("====== 데일리 통계 배치 시작 ======");

            // RDB에서 데이터 집계
            List<StatusCountProjection> projections = readProjectPort.countProjectsByProjectStatus();
            Map<ProjectStatus, Long> counts = projections.stream()
                .collect(Collectors.toMap(StatusCountProjection::getProjectStatus, StatusCountProjection::getCount));

            long total = counts.values().stream().mapToLong(Long::longValue).sum();

            // 2. 엔티티 생성
            DailyStatisticsEntity entity = DailyStatisticsEntity.builder()
                .statDate(LocalDate.now())
                .totalCount(total)
                .inProgressCount(counts.getOrDefault(ProjectStatus.IN_PROGRESS, 0L))
                .dueSoonCount(counts.getOrDefault(ProjectStatus.DUE_SOON, 0L))
                .delayCount(counts.getOrDefault(ProjectStatus.DELAY, 0L))
                .completedCount(counts.getOrDefault(ProjectStatus.COMPLETED, 0L))
                .build();

            dailyStatisticsRepository.save(entity);
            log.info("통계 데이터 RDB에 저장: {}", entity.getStatDate());

            // 레디스에 저장
            String redisKey = cacheKeyProperties.getDashboard() + ":stats:" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String jsonResult = objectMapper.writeValueAsString(entity);
            redisTemplate.opsForValue().set(redisKey, jsonResult);
            log.info("====== 통계 데이터 레디스에 저장, 키: {}", redisKey);

            return RepeatStatus.FINISHED;
        };
    }
}
