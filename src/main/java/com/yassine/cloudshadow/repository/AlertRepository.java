package com.yassine.cloudshadow.repository;


import com.yassine.cloudshadow.entity.Alert;
import com.yassine.cloudshadow.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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
}