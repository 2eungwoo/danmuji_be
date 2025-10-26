package com.back2basics;

import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public interface SoftDeletableCleaner {

    String getName();

    NumberPath<Long> getIdPath();

    Function<Long, Long> getIdExtractor();

    Function<JPAQueryFactory, JPAQuery<Long>> getQueryFunction(LocalDateTime threshold);

    void bulkDelete(List<Long> ids);

    void clean(LocalDateTime threshold);
}