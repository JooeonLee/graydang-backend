package com.graydang.app.domain.user.repository;

import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    Optional<UserProfile> findByUserId(Long userId);

    Optional<UserProfile> findByUserIdAndStatus(Long userId, String status);

    Long user(User user);
}