package com.graydang.app.domain.user.model;

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

    @Column(nullable = false, length = 255)
    private String status;

    // 연관관계 Mapping
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
