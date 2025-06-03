package com.graydang.app.domain.user.repository;

import com.graydang.app.domain.user.model.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    
    Optional<UserCredential> findByProviderAndProviderUserId(String provider, String providerUserId);
} 