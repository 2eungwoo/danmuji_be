package com.back2basics.cleaner;

import com.back2basics.SoftDeletableCleaner;
import com.back2basics.adapter.persistence.board.post.PostEntityRepository;
import com.back2basics.adapter.persistence.board.post.QPostEntity;
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
public class PostCleaner implements SoftDeletableCleaner {

    private final PostEntityRepository repository;

    @Override
    public String getName() {
        return "Post";
    }

    @Override
    public Function<JPAQueryFactory, Predicate> getPredicate(LocalDateTime threshold) {
        return queryFactory -> QPostEntity.postEntity.deletedAt.isNotNull()
            .and(QPostEntity.postEntity.deletedAt.before(threshold));
    }

    @Override
    public Function<JPAQueryFactory, OrderSpecifier<Long>> getOrderSpecifier() {
        return queryFactory -> QPostEntity.postEntity.id.asc();
    }

    @Override
    public Function<JPAQueryFactory, ComparableExpressionBase<Long>> getIdExpression() {
        return queryFactory -> QPostEntity.postEntity.id;
    }

    @Override
    public void bulkDelete(List<Long> ids) {
        repository.bulkDeleteByIds(ids);
    }
}
