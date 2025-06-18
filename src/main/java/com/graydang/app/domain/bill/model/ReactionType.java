package com.graydang.app.domain.bill.model;

import com.graydang.app.domain.bill.exception.BillReactionException;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ReactionType {

    EXCITING("흥미진진"),
    NEEDS_IMPROVEMENT("개선필요"),
    DISAPPOINTED("아쉬워요"),
    LIKE("좋아요");

    private final String displayName;

    ReactionType(String displayName) {
        this.displayName = displayName;
    }

    public static ReactionType from(String name) {
        return Arrays.stream(values())
                .filter(r -> r.displayName.equals(name))
                .findFirst()
                .orElseThrow(() -> new BillReactionException(BaseResponseStatus.INVALID_REACTION_TYPE));
    }
}
