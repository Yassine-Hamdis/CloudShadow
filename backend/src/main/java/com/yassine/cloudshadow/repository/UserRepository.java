package com.yassine.cloudshadow.repository;

import com.yassine.cloudshadow.enums.Role;
import com.yassine.cloudshadow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // For login / auth
    Optional<User> findByEmail(String email);

    // Check duplicate email
    boolean existsByEmail(String email);

    // Fetch all users of a company
    List<User> findAllByCompanyId(Long companyId);

    // Fetch users by role within a company
    List<User> findAllByCompanyIdAndRole(Long companyId, Role role);
}