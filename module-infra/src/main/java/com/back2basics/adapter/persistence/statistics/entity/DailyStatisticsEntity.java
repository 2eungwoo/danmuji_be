package com.back2basics.adapter.persistence.statistics.entity;

import com.back2basics.adapter.persistence.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "daily_statistics",
    uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyStatisticsEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "total_count", nullable = false)
    private long totalCount;

    @Column(name = "in_progress_count", nullable = false)
    private long inProgressCount;

    @Column(name = "due_soon_count", nullable = false)
    private long dueSoonCount;

    @Column(name = "delay_count", nullable = false)
    private long delayCount;

    @Column(name = "completed_count", nullable = false)
    private long completedCount;

    @Column(name = "hold_count", nullable = false)
    private long holdCount;

    @Builder
    public DailyStatisticsEntity(Long id, LocalDate statDate, long totalCount, long inProgressCount, long dueSoonCount, long delayCount, long completedCount, long holdCount) {
        this.id = id;
        this.statDate = statDate;
        this.totalCount = totalCount;
        this.inProgressCount = inProgressCount;
        this.dueSoonCount = dueSoonCount;
        this.delayCount = delayCount;
        this.completedCount = completedCount;
        this.holdCount = holdCount;
    }
}
