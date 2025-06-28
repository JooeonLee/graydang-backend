package com.graydang.app.domain.bill.repository.projection;

import com.graydang.app.domain.bill.model.ReactionType;

public interface BillReactionCountProjection {

    ReactionType getReactionType();
    Long getCount();
}
