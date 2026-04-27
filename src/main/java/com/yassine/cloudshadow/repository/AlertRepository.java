package com.yassine.cloudshadow.repository;


import com.yassine.cloudshadow.entity.Alert;
import com.yassine.cloudshadow.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // All alerts for a specific server
    List<Alert> findAllByServerId(Long serverId);

    // All alerts for all servers of a company
    @Query("SELECT a FROM Alert a WHERE a.server.company.id = :companyId")
    List<Alert> findAllByCompanyId(@Param("companyId") Long companyId);

    // Filter alerts by severity for a company
    @Query("SELECT a FROM Alert a WHERE a.server.company.id = :companyId " +
            "AND a.severity = :severity")
    List<Alert> findAllByCompanyIdAndSeverity(
            @Param("companyId") Long companyId,
            @Param("severity") Severity severity
    );

    // Filter alerts by type (CPU / Memory / Disk) for a company
    @Query("SELECT a FROM Alert a WHERE a.server.company.id = :companyId " +
            "AND a.type = :type")
    List<Alert> findAllByCompanyIdAndType(
            @Param("companyId") Long companyId,
            @Param("type") String type
    );

    // Count unresolved critical alerts per company (for dashboard badge)
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.server.company.id = :companyId " +
            "AND a.severity = 'CRITICAL'")
    long countCriticalAlertsByCompanyId(@Param("companyId") Long companyId);


    // ── NEW: AI alert spam prevention ────────────────────────────────────
    // Checks if AI already created same type alert recently
    // Prevents flooding the frontend with repeated AI alerts
    @Query("SELECT COUNT(a) > 0 FROM Alert a WHERE " +
            "a.server.id = :serverId AND " +
            "a.type = :type AND " +
            "a.isAiGenerated = true AND " +
            "a.timestamp > :since")
    boolean existsRecentAiAlert(
            @Param("serverId") Long serverId,
            @Param("type") String type,
            @Param("since") LocalDateTime since);

    // ── NEW: AI source filter for frontend ───────────────────────────────
    // Frontend can filter "AI Alerts Only"
    @Query("SELECT a FROM Alert a WHERE a.server.company.id = :companyId " +
            "AND a.isAiGenerated = true")
    List<Alert> findAllAiAlertsByCompanyId(@Param("companyId") Long companyId);

}