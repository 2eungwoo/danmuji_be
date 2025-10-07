package com.back2basics.job.projectstatus;

import com.back2basics.project.model.Project;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectStatusUpdateWriter {

    private final EntityManagerFactory entityManagerFactory;

    public ProjectStatusUpdateWriter(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean(name = "projectStatusUpdateItemWriter")
    public JpaItemWriter<Project> writer() {
        return new JpaItemWriterBuilder<Project>()
            .entityManagerFactory(entityManagerFactory)
            .build();
    }
}
