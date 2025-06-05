package com.graydang.app.domain.bill.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Committee {
    FOREIGN_AFFAIRS("외교통일위원회"),
    CULTURE_SPORTS("문화체육관광위원회"),
    HEALTH_WELFARE("보건복지위원회"),
    ENVIRONMENT_LABOR("환경노동위원회"),
    LAND_TRANSPORT("국토교통위원회"),
    EDUCATION("교육위원회"),
    SCIENCE_TECH("과학기술정보방송통신위원회"),
    AGRICULTURE("농림축산식품해양수산위원회"),
    STRATEGY_FINANCE("기획재정위원회"),
    ADMINISTRATION("정무위원회"),
    SAFETY_MANAGEMENT("행정안전위원회"),
    INDUSTRY("산업통상자원중소벤처기업위원회"),
    DEFENSE("국방위원회"),
    LEGISLATION("법제사법위원회"),
    NATIONAL_ASSEMBLY("국회운영위원회"),
    INTELLIGENCE("정보위원회"),
    BUDGET("예산결산특별위원회"),
    ETC("기타");

    private final String label;
}
