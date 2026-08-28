package com.mygaadi.repository;

import com.mygaadi.model.entity.User;
import com.mygaadi.model.enums.Role;
import com.mygaadi.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    long countByRoleAndDeletedFalse(Role role);
    long countByStatusAndDeletedFalse(UserStatus status);

    @Query("select u from User u where u.deleted = false and (:q is null or lower(u.name) like lower(concat('%', :q, '%')) or lower(u.email) like lower(concat('%', :q, '%'))) and (:role is null or u.role = :role)")
    Page<User> search(@Param("q") String q, @Param("role") Role role, Pageable pageable);
}
