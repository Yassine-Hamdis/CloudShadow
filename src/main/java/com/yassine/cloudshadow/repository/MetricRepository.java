package com.yassine.cloudshadow.repository;


import com.yassine.cloudshadow.entity.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {

    // All metrics for a specific server
    List<Metric> findAllByServerId(Long serverId);

    // Metrics for a server within a time range (for graphs/trends)
    List<Metric> findAllByServerIdAndTimestampBetween(
            Long serverId,
            LocalDateTime from,
            LocalDateTime to
    );

    // All metrics for all servers of a company
    // (joins through server → company)
    @Query("SELECT m FROM Metric m WHERE m.server.company.id = :companyId")
    List<Metric> findAllByCompanyId(@Param("companyId") Long companyId);

    // All metrics for a company within a time range
    @Query("SELECT m FROM Metric m WHERE m.server.company.id = :companyId " +
            "AND m.timestamp BETWEEN :from AND :to")
    List<Metric> findAllByCompanyIdAndTimestampBetween(
            @Param("companyId") Long companyId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Latest metric per server (for dashboard summary)
    @Query("SELECT m FROM Metric m WHERE m.server.id = :serverId " +
            "ORDER BY m.timestamp DESC LIMIT 1")
    Metric findLatestByServerId(@Param("serverId") Long serverId);


    // ── NEW: Last N metrics for AI analysis ──────────────────────────────
    // Used by AiAlertService to get recent history per server
    // Native query because JPQL doesn't support LIMIT with parameters
    @Query(value = "SELECT * FROM metrics WHERE server_id = :serverId " +
            "ORDER BY timestamp DESC LIMIT :limit",
            nativeQuery = true)
    List<Metric> findLastNByServerId(
            @Param("serverId") Long serverId,
            @Param("limit") int limit);
}