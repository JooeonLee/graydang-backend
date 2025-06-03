package com.graydang.app.domain.user.repository;

import com.graydang.app.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    @Query("SELECT u FROM User u JOIN u.credentials c WHERE c.provider = :provider AND c.providerUserId = :providerUserId")
    Optional<User> findByProviderAndProviderUserId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);
    
    boolean existsByUsername(String username);
} 