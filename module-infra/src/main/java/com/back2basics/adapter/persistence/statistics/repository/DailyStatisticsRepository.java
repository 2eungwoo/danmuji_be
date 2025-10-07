package com.back2basics.adapter.persistence.statistics.repository;

import com.back2basics.adapter.persistence.statistics.entity.DailyStatisticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyStatisticsRepository extends JpaRepository<DailyStatisticsEntity, Long> {
}
