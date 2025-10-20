package com.back2basics.adapter.persistence.user.repository;

import com.back2basics.adapter.persistence.user.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Long>,
    JpaSpecificationExecutor<UserEntity> {

    boolean existsByUsername(String username);

    Optional<UserEntity> findByUsername(String username);

    @Query("select u.id from UserEntity u where u.name = :name")
    Optional<Long> findIdByName(@Param("name") String name);

    List<UserEntity> findAllByCompany_IdAndDeletedAtIsNull(Long companyId);

    Page<UserEntity> findAllByCompany_IdAndDeletedAtIsNull(Long companyId, Pageable pageable);

    Page<UserEntity> findAllByDeletedAtIsNullOrderByIdDesc(Pageable pageable);

    List<UserEntity> findAllByDeletedAtIsNull();

    Page<UserEntity> findAllByDeletedAtIsNotNull(Pageable pageable);

    @Query("SELECT u FROM UserEntity u WHERE u.username IN :usernames")
    List<UserEntity> findAllByUsernames(List<String> usernames);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.deletedAt IS NULL")
    Long getUserCounts();

    @Query("SELECT DISTINCT u.position FROM UserEntity u WHERE u.deletedAt IS NULL")
    List<String> getUserPositions();

    @Override
    @EntityGraph(attributePaths = "company")
    Page<UserEntity> findAll(Specification<UserEntity> spec, Pageable pageable);

    void deleteByDeletedAtBefore(LocalDateTime threshold);

    void deleteAllByCompanyIdIn(List<Long> deletedCompanyIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserEntity u WHERE u.id IN :ids")
    void bulkDeleteByIds(@Param("ids") List<Long> ids);
}
