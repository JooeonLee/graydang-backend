package com.graydang.app.domain.user.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawalReason {
    NO_LONGER_NEEDED("서비스를 더 이상 사용하지 않아요"),
    LACK_OF_FEATURES("필요한 기능이 부족해요"),
    SWITCHING_SERVICE("다른 서비스를 사용하려고 해요"),
    INCONVENIENT("서비스 이용 중 불편을 느꼈어요"),
    OTHER("기타");
    
    private final String description;
}