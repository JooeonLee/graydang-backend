package com.graydang.app.global.common.util;

import com.graydang.app.global.common.model.enums.TimeUnit;
import lombok.NoArgsConstructor;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@NoArgsConstructor
public class DateUtil {

    public static LocalDate convertStringToDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try {
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException();
        }
    }

    public static int calculateAge(LocalDate birthDate) {
        if(birthDate == null) {
            throw new IllegalArgumentException();
        }
        LocalDate currentDate = LocalDate.now();
        if(birthDate.isAfter(currentDate)) {
            throw new IllegalArgumentException();
        }

        return Period.between(birthDate, currentDate).getYears();
    }

    // 두 날짜 사이의 일 수 계산
    public static long calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    // 두 날짜 사이의 월 수 계산
    public static long calculateMonthsBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

    // 두 날짜 사이의 연 수 계산
    public static long calculateYearsBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.YEARS.between(startDate, endDate);
    }

    // 현재 날짜와 차이 계산
    public static String formatElapsedTime(LocalDate pastDate) {
        return TimeUnit.formatElapsedTime(pastDate);
    }
}
