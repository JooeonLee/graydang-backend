package com.graydang.app.domain.bill.model;

import com.graydang.app.global.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class BillStatusHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관관계 매핑 (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @Column(name = "step_name", nullable = false, length = 255)
    private String stepName;

    @Column(name = "step_result", length = 255)
    private String stepResult;

    @Column(name = "step_order")
    private Integer stepOrder;

    @Column(name = "step_date")
    private LocalDate stepDate;

    @Column(nullable = false, length = 255)
    private String status;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(LocalDate stepDate, String stepResult, String status) {
        this.stepDate = stepDate;
        this.stepResult = stepResult;
        this.status = status;
    }
}