package com.graydang.app.global.common.model.enums;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public enum TimeUnit {

    YEAR(ChronoUnit.YEARS, "년전"),
    MONTH(ChronoUnit.MONTHS, "개월전"),
    DAY(ChronoUnit.DAYS, "일전");

    private final ChronoUnit unit;
    private final String label;

    TimeUnit(ChronoUnit unit, String label) {
        this.unit = unit;
        this.label = label;
    }

    public String format(long value) {
        return value + label;
    }

    public static String formatElapsedTime(LocalDate pastDate) {
        LocalDate now = LocalDate.now();

        return Arrays.stream(values())
                .map(timeUnit -> new TimeDifference(timeUnit.unit.between(pastDate, now), timeUnit))
                .filter(diff -> diff.value() > 0)
                .findFirst()
                .map(TimeDifference::formatted)
                .orElse("오늘");
    }
}
