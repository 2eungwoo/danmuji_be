package com.back2basics.cleaner;

import com.back2basics.SoftDeletableCleaner;
import com.back2basics.adapter.persistence.answer.AnswerEntityRepository;
import com.back2basics.adapter.persistence.answer.QAnswerEntity;
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
public class AnswerCleaner implements SoftDeletableCleaner {

    private final AnswerEntityRepository repository;

    @Override
    public String getName() {
        return "Answer";
    }

    @Override
    public NumberPath<Long> getIdPath() {
        return QAnswerEntity.answerEntity.id;
    }

    @Override
    public Function<Long, Long> getIdExtractor() {
        return id -> id;
    }

    @Override
    public Function<JPAQueryFactory, JPAQuery<Long>> getQueryFunction(LocalDateTime threshold) {
        return queryFactory -> queryFactory
            .select(QAnswerEntity.answerEntity.id)
            .from(QAnswerEntity.answerEntity)
            .where(QAnswerEntity.answerEntity.deletedAt.isNotNull()
                .and(QAnswerEntity.answerEntity.deletedAt.before(threshold)));
    }

    @Override
    public void bulkDelete(List<Long> ids) {
        repository.bulkDeleteByIds(ids);
    }

    @Override
    public void clean(LocalDateTime threshold) {
        repository.deleteByDeletedAtBefore(threshold);
    }
}
