package com.graydang.app.domain.bill.model;

import com.graydang.app.global.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bill")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Bill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_id", nullable = false, length = 255)
    @Comment("OpenAPI 의안 고유 ID")
    private String billId;

    @Column(nullable = false, length = 255)
    @Comment("의안 제목")
    private String title;

    @Column(name = "propose_date")
    @Comment("제안일")
    private java.time.LocalDate proposeDate;

    @Column(name = "committee_name", length = 255)
    @Comment("소관 위원회")
    private String committeeName;

    @Column(name = "process_result", length = 255)
    @Comment("가결, 부결 등")
    private String processResult;

    @Column(name = "bill_status", length = 255)
    @Comment("진행중, 공포, 부결 등")
    private String billStatus;

    @Column(columnDefinition = "TEXT")
    @Comment("의안 요약 정보")
    private String summary;

    @Column(name = "representative_name", length = 255)
    @Comment("대표 발의자 이름")
    private String representativeName;

    @Column(name = "ai_title", length = 255)
    @Comment("AI 생성 제목")
    private String aiTitle;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    @Comment("AI 요약")
    private String aiSummary;

    @Column(name = "ai_processed")
    @Comment("AI 처리 여부")
    private boolean aiProcessed;

    @Column(nullable = false, length = 255)
    @Comment("시스템 관리 상태")
    private String status;

    // 연관관계 Mapping
    @OneToOne(mappedBy = "bill", cascade = CascadeType.ALL)
    private BillVoteResult billVoteResult; // nullable 허용

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, LocalDate proposeDate, String committeeName,
                       String processResult, String billStatus, String summary, String representativeName) {
        this.title = title;
        this.proposeDate = proposeDate;
        this.committeeName = committeeName;
        this.processResult = processResult;
        this.billStatus = billStatus;
        this.summary = summary;
        this.representativeName = representativeName;
    }

    public void updateCommitteeName(String committeeName) {
        this.committeeName = committeeName;
    }

    public void updateAiSummary(String aiTitle, String aiSummary) {
        this.aiTitle = aiTitle;
        this.aiSummary = aiSummary;
        this.aiProcessed = true;
    }

    public void registerRepresentative(String representativeName) {
        if (this.representativeName != null && !this.representativeName.isBlank()) return;
        if (representativeName == null || representativeName.isBlank()) return;

        this.representativeName = representativeName;
    }

    public void updateBillStatus(String status) {
        this.billStatus = status;
    }
}