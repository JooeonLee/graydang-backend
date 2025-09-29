package com.graydang.app.domain.user.model;

import com.graydang.app.domain.user.model.enums.UserStatus;
import com.graydang.app.global.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_credential")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserCredential extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String provider;

    @Column(nullable = false, length = 255)
    private String providerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    // 연관관계 Mapping
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

}
