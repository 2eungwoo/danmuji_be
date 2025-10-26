package com.back2basics.cleaner;

import com.back2basics.SoftDeletableCleaner;
import com.back2basics.adapter.persistence.project.ProjectEntityRepository;
import com.back2basics.adapter.persistence.project.QProjectEntity;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectCleaner implements SoftDeletableCleaner {

    private final ProjectEntityRepository repository;

    @Override
    public String getName() {
        return "Project";
    }

    @Override
    public NumberPath<Long> getIdPath() {
        return QProjectEntity.projectEntity.id;
    }

    @Override
    public Function<Long, Long> getIdExtractor() {
        return id -> id;
    }

    @Override
    public Function<JPAQueryFactory, JPAQuery<Long>> getQueryFunction(LocalDateTime threshold) {
        return queryFactory -> queryFactory
            .select(QProjectEntity.projectEntity.id)
            .from(QProjectEntity.projectEntity)
            .where(QProjectEntity.projectEntity.deletedAt.isNotNull()
                .and(QProjectEntity.projectEntity.deletedAt.before(threshold)));
    }

    @Override
    public void bulkDelete(List<Long> ids) {
        repository.deleteByIdIn(ids);
    }

    @Override
    public void clean(LocalDateTime threshold) {
        List<Long> idsToDelete = repository.findIdsByDeletedAtBefore(threshold);
        if (!idsToDelete.isEmpty()) {
            repository.deleteByIdIn(idsToDelete);
        }
    }
}
