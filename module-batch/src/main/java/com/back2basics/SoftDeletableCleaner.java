package com.back2basics;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public interface SoftDeletableCleaner {

    String getName(); // 로깅용

    // QueryDSL No-Offset ItemReader를 위한 정보 제공
    Function<JPAQueryFactory, Predicate> getPredicate(LocalDateTime threshold);
    Function<JPAQueryFactory, OrderSpecifier<Long>> getOrderSpecifier(); // ID가 Long이라고 가정
    Function<JPAQueryFactory, ComparableExpressionBase<Long>> getIdExpression(); // ID가 Long이라고 가정
    
    // 벌크 삭제를 위한 메서드 (ItemWriter에서 호출될 예정)
    void bulkDelete(List<Long> ids);
}