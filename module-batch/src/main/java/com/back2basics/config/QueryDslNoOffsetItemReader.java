package com.back2basics.config;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.persistence.EntityManager;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.util.ClassUtils;

public class QueryDslNoOffsetItemReader<ID extends Comparable> extends AbstractPagingItemReader<ID> {

    private final EntityManagerFactory entityManagerFactory;
    private final Function<JPAQueryFactory, Predicate> whereClause;
    private final Function<JPAQueryFactory, OrderSpecifier<ID>> orderSpecifier;
    private final Function<JPAQueryFactory, ComparableExpressionBase<ID>> idExpression;

    private ID lastId;

    public QueryDslNoOffsetItemReader(EntityManagerFactory entityManagerFactory,
                                      Function<JPAQueryFactory, Predicate> whereClause,
                                      Function<JPAQueryFactory, OrderSpecifier<ID>> orderSpecifier,
                                      Function<JPAQueryFactory, ComparableExpressionBase<ID>> idExpression,
                                      int pageSize) {
        this.entityManagerFactory = entityManagerFactory;
        this.whereClause = whereClause;
        this.orderSpecifier = orderSpecifier;
        this.idExpression = idExpression;
        setPageSize(pageSize);
        setName(ClassUtils.getShortName(QueryDslNoOffsetItemReader.class));
    }

    @Override
    protected void doReadPage() {
        if (results == null) {
            results = new ArrayList<>();
        } else {
            results.clear();
        }

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        List<ID> fetchedIds = queryFactory.select(idExpression.apply(queryFactory))
            .from(idExpression.apply(queryFactory).getMetadata().getParent()) // This gets the QEntity path
            .where(whereClause.apply(queryFactory), lastId != null ? idExpression.apply(queryFactory).gt(lastId) : null)
            .orderBy(orderSpecifier.apply(queryFactory))
            .limit(getPageSize())
            .fetch();

        results.addAll(fetchedIds);

        if (!results.isEmpty()) {
            lastId = results.get(results.size() - 1);
        }

        entityManager.close();
    }

    @Override
    protected void doJumpToPage(int itemIndex) {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }
}
