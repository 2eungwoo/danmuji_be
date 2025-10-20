package com.back2basics.cleaner;

import com.back2basics.SoftDeletableCleaner;
import com.back2basics.adapter.persistence.inquiry.InquiryEntityRepository;
import com.back2basics.adapter.persistence.inquiry.QInquiryEntity;
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
public class InquiryCleaner implements SoftDeletableCleaner {

    private final InquiryEntityRepository repository;

    @Override
    public String getName() {
        return "Inquiry";
    }

    @Override
    public Function<JPAQueryFactory, Predicate> getPredicate(LocalDateTime threshold) {
        return queryFactory -> QInquiryEntity.inquiryEntity.deletedAt.isNotNull()
            .and(QInquiryEntity.inquiryEntity.deletedAt.before(threshold));
    }

    @Override
    public Function<JPAQueryFactory, OrderSpecifier<Long>> getOrderSpecifier() {
        return queryFactory -> QInquiryEntity.inquiryEntity.id.asc();
    }

    @Override
    public Function<JPAQueryFactory, ComparableExpressionBase<Long>> getIdExpression() {
        return queryFactory -> QInquiryEntity.inquiryEntity.id;
    }

    @Override
    public void bulkDelete(List<Long> ids) {
        repository.deleteByIdIn(ids);
    }
}
