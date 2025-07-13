package com.graydang.app.domain.bill.service;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.bill.model.dto.BillSearchResponseDto;
import com.graydang.app.domain.bill.model.dto.BillSimpleResponseDto;
import com.graydang.app.global.common.model.dto.SliceResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BillSearchService {
    BillSearchResponseDto search(CustomUserDetails userDetails, String keyword, Pageable pageable);
}
