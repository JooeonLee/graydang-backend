package com.graydang.app.domain.bill.service;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.bill.model.dto.BillRecommendResponseDto;
import com.graydang.app.domain.bill.model.dto.BillSimpleResponseDto;
import com.graydang.app.domain.user.model.InterestKeyword;
import com.graydang.app.domain.user.service.UserProfileService;
import com.graydang.app.global.common.model.dto.SliceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillApplicationService {

    private final UserProfileService userProfileService;
    private final BillService billService;

    public BillRecommendResponseDto getBillsByUserKeywords(CustomUserDetails userDetails, Pageable pageable) {

        Long userId = userDetails != null ? userDetails.getUser().getId() : null;

        Set<String> userKeywords;
        String userNickname;
        if(userId == null) {
            userKeywords = InterestKeyword.getRandomLabels();
            userNickname = null;
        }
        else {
            userKeywords = userProfileService.getUserKeywordsByUserId(userId);
            log.info("userKeywords: {}", userKeywords);
            userNickname = userProfileService.getNicknameByUserId(userId);
        }

        SliceResponse<BillSimpleResponseDto> bills = billService.getBillsByKeywordLabels(userDetails, userKeywords, pageable, "proposeDate");

        return BillRecommendResponseDto.of(userNickname, userKeywords, bills);
    }
}
