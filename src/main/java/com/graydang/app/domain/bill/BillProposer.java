package com.graydang.app.domain.bill;

import com.graydang.app.global.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class BillProposer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관관계 매핑 (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String hanjaName;

    @Column(length = 255)
    private String partyName;

    @Column(nullable = false)
    private boolean isRepresentative;

    @Column(nullable = false, length = 255)
    private String status;
}
