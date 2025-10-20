package com.back2basics.cleaner;

import com.back2basics.SoftDeletableCleaner;
import com.back2basics.adapter.persistence.user.repository.UserEntityRepository;
import com.back2basics.adapter.persistence.user.entity.QUserEntity;
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
public class UserCleaner implements SoftDeletableCleaner {

    private final UserEntityRepository repository;

    @Override
    public String getName() {
        return "User";
    }

    @Override
    public Function<JPAQueryFactory, Predicate> getPredicate(LocalDateTime threshold) {
        return queryFactory -> QUserEntity.userEntity.deletedAt.isNotNull()
            .and(QUserEntity.userEntity.deletedAt.before(threshold));
    }

    @Override
    public Function<JPAQueryFactory, OrderSpecifier<Long>> getOrderSpecifier() {
        return queryFactory -> QUserEntity.userEntity.id.asc();
    }

    @Override
    public Function<JPAQueryFactory, ComparableExpressionBase<Long>> getIdExpression() {
        return queryFactory -> QUserEntity.userEntity.id;
    }

    @Override
    public void bulkDelete(List<Long> ids) {
        repository.bulkDeleteByIds(ids);
    }
}
