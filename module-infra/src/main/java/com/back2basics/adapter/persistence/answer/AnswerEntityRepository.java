package com.back2basics.adapter.persistence.answer;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AnswerEntityRepository extends JpaRepository<AnswerEntity, Long> {

    Page<AnswerEntity> findAllByInquiryIdAndDeletedAtIsNull(Long inquiryId, Pageable pageable);

    void deleteAllByInquiryIdIn(List<Long> deletedInquiryIds);

    void deleteByDeletedAtBefore(LocalDateTime threshold);

    @Modifying
    @Transactional
    @Query("DELETE FROM AnswerEntity a WHERE a.id IN :ids")
    void bulkDeleteByIds(@Param("ids") List<Long> ids);
}
