package com.graydang.app.domain.search.model;

import com.graydang.app.domain.common.Yn;
import com.graydang.app.global.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "searchKeyword")
@Comment("검색 키워드")
public class SearchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("PK")
    @Column(name = "id", columnDefinition = "bigint unsigned")
    private Long id;

    @Comment("우선순위(숫자 작을수록 우선)")
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private int priority = 0;

    @Comment("검색 키워드")
    @Column(name = "text", nullable = false, length = 255)
    private String text;

    @Enumerated(EnumType.STRING)
    @Comment("표시 여부")
    @Column(
        name = "displayYn",
        nullable = false,
        columnDefinition = "enum('Y','N') default 'Y'"
    )
    @Builder.Default
    private Yn displayYn = Yn.Y;

    @Enumerated(EnumType.STRING)
    @Comment("삭제 여부(소프트 삭제)")
    @Column(
        name = "delYn",
        nullable = false,
        columnDefinition = "enum('Y','N') default 'N'"
    )
    @Builder.Default
    private Yn delYn = Yn.N;

    @Comment("생성 시각")
    @Column(
        name = "createdAt",
        nullable = false,
        columnDefinition = "datetime(6) default current_timestamp(6)",
        updatable = false,
        insertable = false
    )
    private LocalDateTime createdAt;

    @Comment("수정 시각")
    @Column(
        name = "updatedAt",
        nullable = false,
        columnDefinition = "datetime(6) default current_timestamp(6) on update current_timestamp(6)",
        updatable = false,
        insertable = false
    )
    private LocalDateTime updatedAt;

    // ===== Command Methods =====

    /** 표시 */
    public void show() { this.displayYn = Yn.Y; }

    /** 비표시 */
    public void hide() { this.displayYn = Yn.N; }

    /** 소프트 삭제 */
    public void softDelete() { this.delYn = Yn.Y; }

    /** 소프트 삭제 복구 */
    public void restore() { this.delYn = Yn.N; }

    // 필요 시 정렬 우선순위 변경
    public void changePriority(int priority) { this.priority = priority; }

    // 키워드 텍스트 수정
    public void changeText(String text) { this.text = text; }
}