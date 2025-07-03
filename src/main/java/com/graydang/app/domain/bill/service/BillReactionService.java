package com.graydang.app.domain.bill.service;

import com.graydang.app.domain.bill.model.BillReaction;
import com.graydang.app.domain.bill.model.dto.BillReactionCountResponseDto;
import com.graydang.app.domain.bill.model.dto.BillReactionResponseDto;
import com.graydang.app.domain.bill.model.dto.BillReactionSimpleResponseDto;
import com.graydang.app.domain.bill.repository.BillReactionRepository;
import com.graydang.app.domain.bill.repository.projection.BillReactionCountProjection;
import com.graydang.app.global.common.model.dto.SliceResponse;
import com.graydang.app.domain.bill.model.Bill;
import com.graydang.app.domain.bill.model.ReactionType;
import com.graydang.app.domain.bill.model.dto.BillReactionRequestDto;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BillReactionService {

    private final BillReactionRepository billReactionRepository;
    private final UserService userService;

    public long getBillReactionCountByUserId(Long userId) {
        return billReactionRepository.countByUserId(userId);
    }

    public SliceResponse<BillReactionSimpleResponseDto> getBillReactionInfoByUserId(Long userId, Pageable pageable) {

        Slice<BillReaction> slice = billReactionRepository.findByUserIdAndStatus(userId, "ACTIVE", pageable);

        List<BillReactionSimpleResponseDto> content = slice.getContent().stream()
                .map(b -> BillReactionSimpleResponseDto.from(b, b.getBill().getAiTitle()))
                .toList();

        return new SliceResponse<>(content, slice.getNumber(), slice.isLast());
    }


    @Transactional
    public BillReactionResponseDto toggleReaction(Long userId, Bill bill, BillReactionRequestDto requestDto) {
        User user = userService.findByIdOrThrow(userId);

        ReactionType requestedType = ReactionType.from(requestDto.reactionType());

        Optional<BillReaction> billReactionOptional = billReactionRepository.findByUserIdAndBillId(userId, bill.getId());

        if(billReactionOptional.isEmpty()) {
            // 신규 반응 등록
            BillReaction newReaction = BillReaction.builder()
                    .user(user)
                    .bill(bill)
                    .reactionType(requestedType)
                    .status("ACTIVE")
                    .build();
            billReactionRepository.save(newReaction);

            return BillReactionResponseDto.of(newReaction);
        }

        BillReaction existing = billReactionOptional.get();

        if(existing.isActive()) {
            if(existing.getReactionType().equals(requestedType)) {
                // 동일한 반응 -> soft delete
                existing.softDelete();

            }
            else {
                existing.changeReactionType(requestedType);
            }
        }
        else {
            // DELETED 상태
            if(existing.getReactionType().equals(requestedType)) {
                // 동일 -> restore
                existing.restore();

            }
            else {
                // 다른 타입 -> 교체 + restore
                existing.changeReactionType(requestedType);

            }
        }
        return BillReactionResponseDto.of(existing);
    }

    public BillReactionCountResponseDto getBillReactionCount(Bill bill, Long userId) {

        List<BillReactionCountProjection> results = billReactionRepository.countReactionsByBillId(bill.getId());

        EnumMap<ReactionType, Long> counts = new EnumMap<>(ReactionType.class);
        for (ReactionType type : ReactionType.values()) {
            counts.put(type, 0L); // 기본값 0 설정
        }

        for (BillReactionCountProjection r : results) {
            counts.put(r.getReactionType(), r.getCount());
        }

        ReactionType userReactionType = null;
        if(userId != null) {
            Optional<BillReaction> reactionOptional = billReactionRepository.findByUserIdAndBillId(userId, bill.getId());
            if(reactionOptional.isPresent()) {
                userReactionType = reactionOptional.get().getReactionType();
            }
        }

        return new BillReactionCountResponseDto(
                counts.get(ReactionType.EXCITED),
                counts.get(ReactionType.NEEDS_IMPROVEMENT),
                counts.get(ReactionType.DISAPPOINTED),
                counts.get(ReactionType.LIKE),
                userReactionType != null ? userReactionType.getDisplayName() : null
        );
    }
}
