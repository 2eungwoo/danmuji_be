package com.back2basics.cleaner;

import com.back2basics.SoftDeletableCleaner;
import com.back2basics.adapter.persistence.user.entity.QUserEntity;
import com.back2basics.adapter.persistence.user.repository.UserEntityRepository;
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
public class UserCleaner implements SoftDeletableCleaner {

    private final UserEntityRepository repository;

    @Override
    public String getName() {
        return "User";
    }

    @Override
    public NumberPath<Long> getIdPath() {
        return QUserEntity.userEntity.id;
    }

    @Override
    public Function<Long, Long> getIdExtractor() {
        return id -> id;
    }

    @Override
    public Function<JPAQueryFactory, JPAQuery<Long>> getQueryFunction(LocalDateTime threshold) {
        return queryFactory -> queryFactory
            .select(QUserEntity.userEntity.id)
            .from(QUserEntity.userEntity)
            .where(QUserEntity.userEntity.deletedAt.isNotNull()
                .and(QUserEntity.userEntity.deletedAt.before(threshold)));
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
