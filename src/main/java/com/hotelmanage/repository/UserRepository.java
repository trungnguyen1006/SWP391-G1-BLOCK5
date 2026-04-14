package com.hotelmanage.repository;


import com.hotelmanage.entity.Enum.UserRole;
import com.hotelmanage.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Page<User> findByRoleNot(UserRole role, Pageable pageable);
    Page<User> findByRoleNotAndUsernameContainingIgnoreCaseOrRoleNotAndEmailContainingIgnoreCase(
            UserRole role1, String username, UserRole role2, String email, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = 'RECEPTIONIST' AND u.deletedAt is null")
    List<User> findReceptionists();

    // Dashboard queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE' AND u.role != 'GUEST' AND u.deletedAt IS NULL")
    Long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER' AND u.status = 'ACTIVE' AND u.deletedAt IS NULL")
    Long countActiveCustomers();

}