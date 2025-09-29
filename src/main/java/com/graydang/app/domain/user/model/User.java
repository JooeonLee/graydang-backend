package com.graydang.app.domain.user.model;

import com.graydang.app.domain.user.model.enums.UserStatus;
import com.graydang.app.global.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String username;

    @Column(nullable = false, length = 255)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("시스템 관리 상태")
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(length = 255)
    private String email;


    // 연관관계 Mapping
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<UserCredential> credentials = new ArrayList<>();

    public void withdraw() {
        this.status = UserStatus.INACTIVE;
    }
    
    public void block() {
        this.status = UserStatus.BLOCKED;
    }
    
    public void unblock() {
        this.status = UserStatus.ACTIVE;
    }

}
