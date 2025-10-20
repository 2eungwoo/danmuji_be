package com.back2basics.cleaner;

import com.back2basics.SoftDeletableCleaner;
import com.back2basics.adapter.persistence.company.CompanyEntityRepository;
import com.back2basics.adapter.persistence.company.QCompanyEntity;
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
public class CompanyCleaner implements SoftDeletableCleaner {

    private final CompanyEntityRepository repository;

    @Override
    public String getName() {
        return "Company";
    }

    @Override
    public NumberPath<Long> getIdPath() {
        return QCompanyEntity.companyEntity.id;
    }

    @Override
    public Function<Long, Long> getIdExtractor() {
        return id -> id;
    }

    @Override
    public Function<JPAQueryFactory, JPAQuery<Long>> getQueryFunction(LocalDateTime threshold) {
        return queryFactory -> queryFactory
            .select(QCompanyEntity.companyEntity.id)
            .from(QCompanyEntity.companyEntity)
            .where(QCompanyEntity.companyEntity.deletedAt.isNotNull()
                .and(QCompanyEntity.companyEntity.deletedAt.before(threshold)));
    }

    @Override
    public void bulkDelete(List<Long> ids) {
        repository.deleteByIdIn(ids);
    }
}
