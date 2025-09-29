package com.graydang.app.domain.user.model;

import com.graydang.app.domain.user.model.enums.UserStatus;
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

    @Column(nullable = true, length = 100)
    private String keyword2;

    @Column(nullable = true,length = 100)
    private String keyword3;

    @Column(nullable = true, length = 100)
    private String keyword4;

    @Column(nullable = true, length = 100)
    private String keyword5;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    // 연관관계 Mapping
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void updateProfileInfo(String nickname, String keyword1, String keyword2, String keyword3, String keyword4, String keyword5) {
        this.nickname = nickname;
        this.keyword1 = keyword1;
        this.keyword2 = keyword2;
        this.keyword3 = keyword3;
        this.keyword4 = keyword4;
        this.keyword5 = keyword5;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
}
