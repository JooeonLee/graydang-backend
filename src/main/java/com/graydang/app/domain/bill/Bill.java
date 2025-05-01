package com.graydang.app.domain.bill;

import com.graydang.app.global.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

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

    @Column(nullable = false, length = 255)
    @Comment("시스템 관리 상태")
    private String status;
}