package com.back2basics.job.projectstatus;

import com.back2basics.project.model.Project;
import com.back2basics.project.model.ProjectStatus;
import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectStatusUpdateReader {

    private final EntityManagerFactory entityManagerFactory;

    public ProjectStatusUpdateReader(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Project> projectReader() {
        String jpqlQuery = "SELECT p FROM Project p WHERE p.status IN (:inProgress, :completed)";

        return new JpaPagingItemReaderBuilder<Project>()
            .name("projectItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString(jpqlQuery)
            .parameterValues(Map.of(
                "inProgress", ProjectStatus.IN_PROGRESS,
                "completed", ProjectStatus.COMPLETED
            ))
            .pageSize(100) // 100개
            .build();
    }
}
