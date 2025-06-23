package com.graydang.app.domain.bill.service;

import com.graydang.app.domain.bill.model.BillReaction;
import com.graydang.app.domain.bill.model.dto.BillReactionResponseDto;
import com.graydang.app.domain.bill.model.dto.BillSimpleResponseDto;
import com.graydang.app.domain.bill.repository.BillReactionRepository;
import com.graydang.app.global.common.model.dto.SliceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class BillReactionService {

    private final BillReactionRepository billReactionRepository;

    public long getBillReactionCountByUserId(Long userId) {
        return billReactionRepository.countByUserId(userId);
    }

    public SliceResponse<BillReactionResponseDto> getBillReactionInfoByUserId(Long userId, Pageable pageable) {

        Slice<BillReaction> slice = billReactionRepository.findByUserIdAndStatus(userId, "ACTIVE", pageable);

        List<BillReactionResponseDto> content = slice.getContent().stream()
                .map(b -> BillReactionResponseDto.from(b, b.getBill().getAiTitle()))
                .toList();

        return new SliceResponse<>(content, slice.getNumber(), slice.isLast());

    }
}
