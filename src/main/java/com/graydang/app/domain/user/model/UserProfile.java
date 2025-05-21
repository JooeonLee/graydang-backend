package com.graydang.app.domain.user.model;

import com.graydang.app.global.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String keyword1;

    @Column(nullable = false, length = 100)
    private String keyword2;

    @Column(nullable = false,length = 100)
    private String keyword3;

    @Column(nullable = false, length = 100)
    private String keyword4;

    @Column(nullable = false, length = 100)
    private String keyword5;

    @Column(nullable = false, length = 255)
    private String status;

    // 연관관계 Mapping
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
