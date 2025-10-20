package com.back2basics.cleaner;

import com.back2basics.SoftDeletableCleaner;
import com.back2basics.adapter.persistence.project.ProjectEntityRepository;
import com.back2basics.adapter.persistence.project.QProjectEntity;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
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
    public Function<JPAQueryFactory, Predicate> getPredicate(LocalDateTime threshold) {
        return queryFactory -> QProjectEntity.projectEntity.deletedAt.isNotNull()
            .and(QProjectEntity.projectEntity.deletedAt.before(threshold));
    }

    @Override
    public Function<JPAQueryFactory, OrderSpecifier<Long>> getOrderSpecifier() {
        return queryFactory -> QProjectEntity.projectEntity.id.asc();
    }

    @Override
    public Function<JPAQueryFactory, ComparableExpressionBase<Long>> getIdExpression() {
        return queryFactory -> QProjectEntity.projectEntity.id;
    }

    @Override
    public void bulkDelete(List<Long> ids) {
        repository.deleteByIdIn(ids);
    }
}
