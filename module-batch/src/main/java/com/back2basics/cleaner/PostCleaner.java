package com.back2basics.cleaner;

import com.back2basics.SoftDeletableCleaner;
import com.back2basics.adapter.persistence.board.post.PostEntityRepository;
import com.back2basics.adapter.persistence.board.post.QPostEntity;
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
public class PostCleaner implements SoftDeletableCleaner {

    private final PostEntityRepository repository;

    @Override
    public String getName() {
        return "Post";
    }

    @Override
    public NumberPath<Long> getIdPath() {
        return QPostEntity.postEntity.id;
    }

    @Override
    public Function<Long, Long> getIdExtractor() {
        return id -> id;
    }

    @Override
    public Function<JPAQueryFactory, JPAQuery<Long>> getQueryFunction(LocalDateTime threshold) {
        return queryFactory -> queryFactory
            .select(QPostEntity.postEntity.id)
            .from(QPostEntity.postEntity)
            .where(QPostEntity.postEntity.deletedAt.isNotNull()
                .and(QPostEntity.postEntity.deletedAt.before(threshold)));
    }

    @Override
    public void bulkDelete(List<Long> ids) {
        repository.bulkDeleteByIds(ids);
    }
}
