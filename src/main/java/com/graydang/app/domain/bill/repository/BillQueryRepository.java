package com.graydang.app.domain.bill.repository;

import com.graydang.app.domain.bill.model.Committee;
import com.graydang.app.domain.bill.model.dto.BillSimpleResponseDto;
import com.graydang.app.domain.bill.repository.projection.BillSimpleProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.Set;

public interface BillQueryRepository {

    Slice<BillSimpleResponseDto> findBillSimpleProjectionByCommittees(Set<String> committeeLabels, Long userId, Pageable pageable, String sortBy);

    Slice<BillSimpleResponseDto> findBySearchKeyword(String keyword, Long userId, Pageable pageable, String sortBy);

    Long countBySearchKeyword(String keyword);
}
