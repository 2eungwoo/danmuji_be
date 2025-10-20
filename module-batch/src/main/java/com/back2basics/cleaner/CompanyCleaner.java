package com.back2basics.cleaner;

import com.back2basics.SoftDeletableCleaner;
import com.back2basics.adapter.persistence.company.CompanyEntityRepository;
import com.back2basics.adapter.persistence.company.QCompanyEntity;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyCleaner implements SoftDeletableCleaner {

    private final CompanyEntityRepository repository;

    @Override
    public String getName() {
        return "Company";
    }

    @Override
    public Function<JPAQueryFactory, Predicate> getPredicate(LocalDateTime threshold) {
        return queryFactory -> QCompanyEntity.companyEntity.deletedAt.isNotNull()
            .and(QCompanyEntity.companyEntity.deletedAt.before(threshold));
    }

    @Override
    public Function<JPAQueryFactory, OrderSpecifier<Long>> getOrderSpecifier() {
        return queryFactory -> QCompanyEntity.companyEntity.id.asc();
    }

    @Override
    public Function<JPAQueryFactory, ComparableExpressionBase<Long>> getIdExpression() {
        return queryFactory -> QCompanyEntity.companyEntity.id;
    }

    @Override
    public void bulkDelete(List<Long> ids) {
        repository.deleteByIdIn(ids);
    }
}
