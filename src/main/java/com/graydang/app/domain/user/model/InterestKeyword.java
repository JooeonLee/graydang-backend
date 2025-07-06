package com.graydang.app.domain.user.model;

import com.graydang.app.domain.bill.model.Committee;
import com.graydang.app.domain.user.exception.UserException;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum InterestKeyword {
    해외("해외", Committee.FOREIGN_AFFAIRS),
    문화예술("문화예술", Committee.CULTURE_SPORTS),
    미디어("미디어", Committee.CULTURE_SPORTS),
    스포츠("스포츠", Committee.CULTURE_SPORTS),
    의료("의료", Committee.HEALTH_WELFARE),
    자연("자연", Committee.ENVIRONMENT_LABOR), // 기존: 자연환경
    근로("근로", Committee.ENVIRONMENT_LABOR),
    부동산("부동산", Committee.LAND_TRANSPORT),
    건축_도시("건축/도시", Committee.LAND_TRANSPORT), // 유사 키워드 → 교통/주택과 같은 위원회
    철도_항공("철도/항공", Committee.LAND_TRANSPORT),
    학교("학교", Committee.EDUCATION),
    과학기술_IT("과학기술/IT", Committee.SCIENCE_TECH), // IT/AI + 과학기술 통합
    농수산물("농수산물", Committee.AGRICULTURE), // 기존: 농수산업
    경제("경제", Committee.STRATEGY_FINANCE),
    세금("세금", Committee.STRATEGY_FINANCE),
    금융("금융", Committee.ADMINISTRATION),
    재난안전("재난안전", Committee.SAFETY_MANAGEMENT),
    선거("선거", Committee.SAFETY_MANAGEMENT),
    무역("무역", Committee.INDUSTRY),
    창업("창업", Committee.INDUSTRY),
    자원에너지("자원에너지", Committee.INDUSTRY),
    국방("국방", Committee.DEFENSE), // 기존: 국방/안보
    재판("재판", Committee.LEGISLATION), // 기존: 법/재판
    정치("정치", Committee.BUDGET),
    가정_아동청소년("가정/아동청소년", Committee.HEALTH_WELFARE), // 새로운 키워드
    성평등("성평등", Committee.HEALTH_WELFARE); // 새로운 키워드

    private final String label;
    private final Committee committee;

    public static InterestKeyword fromLabel(String label) {
        return Arrays.stream(values())
                .filter(k -> k.label.equals(label))
                .findFirst()
                .orElseThrow(() -> new UserException(BaseResponseStatus.INVALID_USER_KEYWORD));
    }

    public static Set<String> convertLabelsToCommitteeLabels(Set<String> labels) {
        return labels.stream()
                .map(InterestKeyword::fromLabel)
                .map(InterestKeyword::getCommittee)
                .map(Committee::getLabel)
                .collect(Collectors.toSet());
    }
}
