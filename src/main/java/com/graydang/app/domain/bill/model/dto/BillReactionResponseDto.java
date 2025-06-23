package com.graydang.app.domain.bill.model.dto;

import com.graydang.app.domain.bill.model.BillReaction;

import java.util.Optional;

public record BillReactionResponseDto(
        boolean hasReacted,
        String reactionType
) {

    public static BillReactionResponseDto of(BillReaction reaction) {
        return Optional.of(reaction)
                .filter(BillReaction::isActive)
                .map(r -> new BillReactionResponseDto(true, r.getReactionType().getDisplayName()))
                .orElse(new BillReactionResponseDto(false, null));
    }
}
