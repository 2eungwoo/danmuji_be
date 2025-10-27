package com.back2basics.config;

import com.back2basics.adapter.persistence.project.QProjectEntity;
import com.back2basics.adapter.persistence.statistics.entity.DailyStatisticsEntity;
import com.back2basics.adapter.persistence.statistics.repository.DailyStatisticsRepository;
import com.back2basics.dto.ProjectStatusDto;
import com.back2basics.global.config.CacheKeyProperties;
import com.back2basics.project.model.ProjectStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
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
    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final CacheKeyProperties cacheKeyProperties;
    private final JPAQueryFactory jpaQueryFactory;

    private static final int CHUNK_SIZE = 1000;
    private static final String REDIS_AGGREGATION_KEY_PREFIX = "daily_stats_agg:";

    @Bean
    public Job dailyStatisticsJob() {
        return new JobBuilder("dailyStatisticsJob", jobRepository)
            .start(cleanupDailyStatisticsStep())
            .next(readProjectsAndAggregateInRedisStep())
            .next(finalizeDailyStatisticsStep())
            .build();
    }

    @Bean
    public Step cleanupDailyStatisticsStep() {
        return new StepBuilder("cleanupDailyStatisticsStep", jobRepository)
            .tasklet(cleanupDailyStatisticsTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet cleanupDailyStatisticsTasklet() {
        return (contribution, chunkContext) -> {
            String todayKey = REDIS_AGGREGATION_KEY_PREFIX + LocalDate.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
            log.info("====== 이전 집계 데이터 삭제, 키: {} ======", todayKey);
            redisTemplate.delete(todayKey);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step readProjectsAndAggregateInRedisStep() {
        return new StepBuilder("readProjectsAndAggregateInRedisStep", jobRepository)
            .<ProjectStatusDto, ProjectStatus>chunk(CHUNK_SIZE, transactionManager)
            .reader(projectStatusDtoNoOffsetReader())
            .processor(projectStatusProcessor())
            .writer(redisAggregationWriter())
            .build();
    }

    @Bean
    public QueryDslNoOffsetItemReader<ProjectStatusDto> projectStatusDtoNoOffsetReader() {
        QProjectEntity project = QProjectEntity.projectEntity;
        return new QueryDslNoOffsetItemReader<>(
            jpaQueryFactory,
            CHUNK_SIZE,
            project.id,
            ProjectStatusDto::getId,
            queryFactory -> queryFactory
                .select(Projections.constructor(ProjectStatusDto.class,
                    project.id,
                    project.projectStatus
                ))
                .from(project)
                .where(project.isDeleted.isFalse())
        );
    }

    @Bean
    public ItemProcessor<ProjectStatusDto, ProjectStatus> projectStatusProcessor() {
        return ProjectStatusDto::getProjectStatus;
    }

    @Bean
    public ItemWriter<ProjectStatus> redisAggregationWriter() {
        return items -> {
            String todayKey = REDIS_AGGREGATION_KEY_PREFIX + LocalDate.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
            for (ProjectStatus status : items) {
                redisTemplate.opsForHash().increment(todayKey, status.name(), 1L);
            }
            redisTemplate.expire(todayKey, 2, TimeUnit.DAYS);
        };
    }

    @Bean
    public Step finalizeDailyStatisticsStep() {
        return new StepBuilder("finalizeDailyStatisticsStep", jobRepository)
            .tasklet(finalizeDailyStatisticsTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet finalizeDailyStatisticsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("====== 데일리 통계 배치 최종 집계 시작 ======");

            String todayKey = REDIS_AGGREGATION_KEY_PREFIX + LocalDate.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
            Map<Object, Object> rawCounts = redisTemplate.opsForHash().entries(todayKey);

            Map<ProjectStatus, Long> counts = new HashMap<>();
            long total = 0L;
            for (Map.Entry<Object, Object> entry : rawCounts.entrySet()) {
                ProjectStatus status = ProjectStatus.valueOf(entry.getKey().toString());
                Long count = Long.valueOf(entry.getValue().toString());
                counts.put(status, count);
                total += count;
            }

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

            String redisCacheKey = cacheKeyProperties.getDashboard() + ":stats:" + LocalDate.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
            String jsonResult = objectMapper.writeValueAsString(entity);
            redisTemplate.opsForValue().set(redisCacheKey, jsonResult);
            log.info("====== 통계 데이터 레디스에 저장, 키: {}", redisCacheKey);

            redisTemplate.delete(todayKey);

            return RepeatStatus.FINISHED;
        };
    }
}
