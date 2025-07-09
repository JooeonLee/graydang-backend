package com.graydang.app.domain.bill.service;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.bill.model.dto.BillSimpleResponseDto;
import com.graydang.app.domain.bill.repository.BillQueryRepository;
import com.graydang.app.global.common.model.dto.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeBillSearchService implements BillSearchService {

    private final BillQueryRepository billQueryRepository;

    @Override
    public SliceResponse<BillSimpleResponseDto> search(CustomUserDetails userDetails, String keyword, org.springframework.data.domain.Pageable pageable) {
        Long userId = userDetails != null ? userDetails.getUser().getId() : null;

        Slice<BillSimpleResponseDto> bills = billQueryRepository.findBySearchKeyword(keyword, userId, pageable, "proposeDate");
        return new SliceResponse<>(bills);
    }
}
