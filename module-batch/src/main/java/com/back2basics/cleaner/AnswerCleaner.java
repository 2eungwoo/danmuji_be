package com.back2basics.cleaner;

import com.back2basics.SoftDeletableCleaner;
import com.back2basics.adapter.persistence.answer.AnswerEntityRepository;
import com.back2basics.adapter.persistence.answer.QAnswerEntity;
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
public class AnswerCleaner implements SoftDeletableCleaner {

    private final AnswerEntityRepository repository;

    @Override
    public String getName() {
        return "Answer";
    }

    @Override
    public Function<JPAQueryFactory, Predicate> getPredicate(LocalDateTime threshold) {
        return queryFactory -> QAnswerEntity.answerEntity.deletedAt.isNotNull()
            .and(QAnswerEntity.answerEntity.deletedAt.before(threshold));
    }

    @Override
    public Function<JPAQueryFactory, OrderSpecifier<Long>> getOrderSpecifier() {
        return queryFactory -> QAnswerEntity.answerEntity.id.asc();
    }

    @Override
    public Function<JPAQueryFactory, ComparableExpressionBase<Long>> getIdExpression() {
        return queryFactory -> QAnswerEntity.answerEntity.id;
    }

    @Override
    public void bulkDelete(List<Long> ids) {
        repository.bulkDeleteByIds(ids);
    }
}
