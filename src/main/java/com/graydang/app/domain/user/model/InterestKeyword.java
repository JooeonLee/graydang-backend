package com.graydang.app.domain.user.model;

import com.graydang.app.domain.bill.model.Committee;
import com.graydang.app.domain.user.exception.UserException;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum InterestKeyword {
    외교("외교", Committee.FOREIGN_AFFAIRS),
    통일("통일", Committee.FOREIGN_AFFAIRS),
    해외("해외", Committee.FOREIGN_AFFAIRS),

    문화예술("문화예술", Committee.CULTURE_SPORTS),
    관광("관광", Committee.CULTURE_SPORTS),
    미디어("미디어", Committee.CULTURE_SPORTS),
    스포츠("스포츠", Committee.CULTURE_SPORTS),

    복지("복지", Committee.HEALTH_WELFARE),
    의료("의료", Committee.HEALTH_WELFARE),

    자연환경("자연환경", Committee.ENVIRONMENT_LABOR),
    근로("근로", Committee.ENVIRONMENT_LABOR),
    화학("화학", Committee.ENVIRONMENT_LABOR),

    부동산("부동산", Committee.LAND_TRANSPORT),
    교통("교통", Committee.LAND_TRANSPORT),
    주택("주택", Committee.LAND_TRANSPORT),

    학교("학교", Committee.EDUCATION),
    입시("입시", Committee.EDUCATION),

    IT_AI("IT/AI", Committee.SCIENCE_TECH),
    과학기술("과학/기술", Committee.SCIENCE_TECH),
    방송("방송", Committee.SCIENCE_TECH),
    우주("우주", Committee.SCIENCE_TECH),
    원자력("원자력", Committee.SCIENCE_TECH),

    농수산업("농수산업", Committee.AGRICULTURE),
    먹거리("먹거리", Committee.AGRICULTURE),

    경제("경제", Committee.STRATEGY_FINANCE),
    세금("세금", Committee.STRATEGY_FINANCE),

    금융("금융", Committee.ADMINISTRATION),

    재난안전("재난안전", Committee.SAFETY_MANAGEMENT),
    선거("선거", Committee.SAFETY_MANAGEMENT),

    무역("무역", Committee.INDUSTRY),
    창업("창업", Committee.INDUSTRY),
    자원에너지("자원에너지", Committee.INDUSTRY),

    국방안보("국방/안보", Committee.DEFENSE),

    법재판("법/재판", Committee.LEGISLATION),

    국회("국회", Committee.NATIONAL_ASSEMBLY),

    정치("정치", Committee.BUDGET), // 정치 관련 키워드는 예산위로 매핑 (정치 위원회는 따로 없어서)

    정보보안("정보보안", Committee.INTELLIGENCE);

    private final String label;
    private final Committee committee;

    public static InterestKeyword fromLabel(String label) {
        return Arrays.stream(InterestKeyword.values())
                .filter(k -> k.label.equals(label))
                .findFirst()
                .orElseThrow(() -> new UserException(BaseResponseStatus.INVALID_USER_KEYWORD));
    }
}
