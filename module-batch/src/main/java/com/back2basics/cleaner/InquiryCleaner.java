package com.back2basics.cleaner;

import com.back2basics.SoftDeletableCleaner;
import com.back2basics.adapter.persistence.inquiry.InquiryEntityRepository;
import com.back2basics.adapter.persistence.inquiry.QInquiryEntity;
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
public class InquiryCleaner implements SoftDeletableCleaner {

    private final InquiryEntityRepository repository;

    @Override
    public String getName() {
        return "Inquiry";
    }

    @Override
    public NumberPath<Long> getIdPath() {
        return QInquiryEntity.inquiryEntity.id;
    }

    @Override
    public Function<Long, Long> getIdExtractor() {
        return id -> id;
    }

    @Override
    public Function<JPAQueryFactory, JPAQuery<Long>> getQueryFunction(LocalDateTime threshold) {
        return queryFactory -> queryFactory
            .select(QInquiryEntity.inquiryEntity.id)
            .from(QInquiryEntity.inquiryEntity)
            .where(QInquiryEntity.inquiryEntity.deletedAt.isNotNull()
                .and(QInquiryEntity.inquiryEntity.deletedAt.before(threshold)));
    }

    @Override
    public void bulkDelete(List<Long> ids) {
        repository.deleteByIdIn(ids);
    }

    @Override
    public void clean(LocalDateTime threshold) {
        List<Long> idsToDelete = repository.findIdsByDeletedAtBefore(threshold);
        if (!idsToDelete.isEmpty()) {
            repository.deleteByIdIn(idsToDelete);
        }
    }
}