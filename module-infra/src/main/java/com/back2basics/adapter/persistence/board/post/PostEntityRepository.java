package com.back2basics.adapter.persistence.board.post;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostEntityRepository extends JpaRepository<PostEntity, Long> {

    void deleteByDeletedAtBefore(LocalDateTime threshold);

    void deleteAllByProjectIdIn(List<Long> deletedProjectIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM PostEntity p WHERE p.id IN :ids")
    void bulkDeleteByIds(@Param("ids") List<Long> ids);
}
