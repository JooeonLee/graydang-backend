package com.graydang.app.domain.bill;

import com.graydang.app.global.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bill_vote_result",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_bill_vote_result", columnNames = "bill_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BillVoteResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vote_date", nullable = false)
    private LocalDate voteDate;

    @Column(name = "agree_count", nullable = false)
    private int agreeCount;

    @Column(name = "oppose_count", nullable = false)
    private int opposeCount;

    @Column(name = "abstention_count", nullable = false)
    private int abstentionCount;

    @Column(name = "absence_count", nullable = false)
    private int absenceCount;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "vote_result", nullable = false)
    private String voteResult;

    @Column(name = "status", nullable = false)
    private String status;

    // 외래키 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(
            LocalDate voteDate,
            int agreeCount,
            int opposeCount,
            int abstentionCount,
            int memberTotalCount,
            int voteTotalCount,
            String voteResult,
            String status
    ) {
        this.voteDate = voteDate;
        this.agreeCount = agreeCount;
        this.opposeCount = opposeCount;
        this.abstentionCount = abstentionCount;
        this.absenceCount = memberTotalCount - voteTotalCount;
        this.totalCount = voteTotalCount;
        this.voteResult = voteResult;
        this.status = status;
    }
}
