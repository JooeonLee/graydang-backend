package com.graydang.app.domain.comment.model;

import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.comment.model.enums.CommentStatus;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.global.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Audited
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isEdited;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    // 연관관계 Mapping
    @ManyToOne(fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
    @NotAudited
    private List<CommentLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
    @NotAudited
    private List<CommentReport> reports = new ArrayList<>();

    public void updateContent(String content) {
        this.content = content;
        this.isEdited = true;
    }

    public void softDelete() {
        this.status = CommentStatus.DELETED;
    }

    public void restore() {
        this.status = CommentStatus.ACTIVE;
    }

    public boolean isActive() {
        return CommentStatus.ACTIVE == this.status;
    }

    public long getActiveLikeCount() {
        return this.likes.stream()
                .filter(CommentLike::isActive)
                .count();
    }
}