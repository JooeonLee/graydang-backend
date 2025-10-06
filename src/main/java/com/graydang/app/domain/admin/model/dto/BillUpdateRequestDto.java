package com.graydang.app.domain.admin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "법안 수정 요청 DTO")
public class BillUpdateRequestDto {

    @Schema(description = "법안 제목", example = "개정된 법안 제목")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다")
    private String title;

    @Schema(description = "법안 요약", example = "수정된 법안 요약 내용")
    private String summary;

    @Schema(description = "AI 생성 제목", example = "AI가 생성한 수정 제목")
    @Size(max = 255, message = "AI 제목은 255자를 초과할 수 없습니다")
    private String aiTitle;

    @Schema(description = "AI 요약", example = "AI가 생성한 수정 요약")
    private String aiSummary;

    @Schema(description = "소관 위원회", example = "기획재정위원회")
    @Size(max = 255, message = "위원회명은 255자를 초과할 수 없습니다")
    private String committeeName;

    @Schema(description = "법안 상태", example = "위원회 심사")
    @Size(max = 255, message = "법안 상태는 255자를 초과할 수 없습니다")
    private String billStatus;
}