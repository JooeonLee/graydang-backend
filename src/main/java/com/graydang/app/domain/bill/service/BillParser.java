package com.graydang.app.domain.bill.service;

public class BillParser {
    /**
     * 법안 제목에서 대표발의 의원 이름을 추출합니다.
     * 예: "형사소송법 일부개정법률안(백혜련의원 등 10인)" → "백혜련"
     * 대표발의 의원 정보가 없으면 null 반환
     */
    public static String extractProposer(String title) {
        if (title == null || !title.contains("(") || !title.contains(")")) {
            return null;
        }

        int lastStart = title.lastIndexOf('(');
        int lastEnd = title.lastIndexOf(')');
        if (lastStart == -1 || lastEnd == -1 || lastEnd <= lastStart) return null;

        String inside = title.substring(lastStart + 1, lastEnd).trim();
        // 예: "김용민의원 등 170인"

        if (!inside.contains("의원")) return null;

        int endOfName = inside.indexOf("의원");
        if (endOfName <= 0) return null;

        return inside.substring(0, endOfName).trim(); // "김용민"
    }
}
