package com.graydang.app.domain.admin.controller;

import com.graydang.app.domain.admin.dto.UserDetailResponseDto;
import com.graydang.app.domain.admin.dto.UserListResponseDto;
import com.graydang.app.domain.admin.service.UserAdminService;
import com.graydang.app.global.common.model.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "어드민 유저 관리", description = "어드민용 유저 조회 및 관리 API")
public class UserAdminController {

    private final UserAdminService userAdminService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 목록 조회", description = "어드민용 유저 목록을 페이징하여 조회합니다.")
    public ResponseEntity<BaseResponse<Page<UserListResponseDto>>> getUserList(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) @Parameter(description = "검색 키워드 (이름, 이메일)") String keyword
    ) {
        Page<UserListResponseDto> userList = userAdminService.getUserList(pageable, keyword);
        return ResponseEntity.ok(BaseResponse.success(userList));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "유저 상세 정보 조회", description = "특정 유저의 상세 정보를 조회합니다.")
    public ResponseEntity<BaseResponse<UserDetailResponseDto>> getUserDetail(
            @PathVariable @Parameter(description = "유저 ID") Long userId
    ) {
        UserDetailResponseDto userDetail = userAdminService.getUserDetail(userId);
        return ResponseEntity.ok(BaseResponse.success(userDetail));
    }
}