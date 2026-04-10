package com.yassine.cloudshadow.repository;

import com.yassine.cloudshadow.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {

    // Agent authenticates using token
    Optional<Server> findByToken(String token);

    // List all servers of a company
    List<Server> findAllByCompanyId(Long companyId);

    // Check duplicate server name within same company
    boolean existsByNameAndCompanyId(String name, Long companyId);

    // Check duplicate token (safety check)
    boolean existsByToken(String token);
}