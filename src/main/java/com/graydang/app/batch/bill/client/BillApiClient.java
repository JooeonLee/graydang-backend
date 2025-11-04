package com.graydang.app.batch.bill.client;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.graydang.app.batch.bill.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillApiClient {

    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper;

    @Value("${openapi.bill.service-key}")
    private String serviceKey;
    @Value("${openapi.bill.service-key-v2}")
    private String serviceKeyV2;

    private final String API_BASE_URL = "https://apis.data.go.kr/9710000/BillInfoService2/";
    private final String API_BASE_URL_V2 = "https://open.assembly.go.kr/portal/openapi/ncocpgfiaoituanbr/";

    public List<BillInfoResponseDto.ItemDto> getBillInfoList(int numOfRows, int pageNo) {
//        String url = API_BASE_URL + "getBillInfoList"
//                + "?serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8)
//                + "&numOfRows=" + numOfRows
//                + "&pageNo=" + pageNo;

//        String url = UriComponentsBuilder
//                .fromHttpUrl(API_BASE_URL + "getBillInfoList")
//                .queryParam("serviceKey", UriUtils.encodeQueryParam(serviceKey, StandardCharsets.UTF_8))
//                .queryParam("pageNo", pageNo)
//                .queryParam("numOfRows", numOfRows)
//                .build(false) // 꼭 true로!
//                .toUriString();


//        String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

        String url = "https://apis.data.go.kr/9710000/BillInfoService2/getBillInfoList"
                + "?serviceKey=" + serviceKey
                + "&pageNo=" + pageNo
                + "&numOfRows=" + numOfRows
                + "&ord=A01&start_ord=22&end_ord=22";

        URI uri = URI.create(url);

        log.info("[DEBUG] 서비스 키: {}", serviceKey);
        log.info(">> 최종 요청 URI: {}", uri);

        try {

            String xml = restTemplate.getForObject(uri, String.class);
            log.info("[DEBUG] XML 응답:\n{}", xml);
            BillInfoResponseDto responseDto = xmlMapper.readValue(xml, BillInfoResponseDto.class);
            return responseDto.getBody().getItems();
        } catch (Exception e) {
            log.error("[API 오류] getBillInfoList 호출 실패", e);
            throw new RuntimeException("getBillInfoList 호출 실패");
        }
    }

    public List<BillRecentResponseDto.ItemDto> getBillRecentList(int numOfRows, int pageNo) {
        String url = API_BASE_URL + "getRecentRceptList"
                + "?serviceKey=" + serviceKey
                + "&numOfRows=" + numOfRows
                + "&pageNo=" + pageNo;

        URI uri = URI.create(url);

        log.info("[DEBUG] 서비스 키: {}", serviceKey);
        log.info(">> 최종 요청 URI: {}", url);

        try {
            String xml = restTemplate.getForObject(URI.create(url), String.class);
            log.info("[DEBUG] XML 응답:\n{}", xml);
            BillRecentResponseDto responseDto = xmlMapper.readValue(xml, BillRecentResponseDto.class);
            return responseDto.getBody().getItems();
        } catch (Exception e) {
            log.error("[API 오류] 최근 등록 의안 조회 실패 - error: {}" , e.getMessage());
            throw new RuntimeException("getBillRecentList 호출 실패");
        }
    }

    public Optional<BillReceiptInfoResponseDto> getBillReceiptInfo(String billId) {
        String url = API_BASE_URL + "getBillReceiptInfo"
                + "?serviceKey=" + serviceKey
                + "&bill_id=" + billId;

        URI uri = URI.create(url);
        log.info("[DEBUG] 서비스 키: {}", serviceKey);
        log.info(">> 최종 요청 URI: {} {}", uri, url);

        try {
            String xml = restTemplate.getForObject(uri, String.class);
            log.info("[DEBUG] XML 응답(요약): {}",  xml != null && xml.length() > 300 ? xml.substring(0, 300) + "..." : xml);
            BillReceiptInfoResponseDto responseDto = xmlMapper.readValue(xml, BillReceiptInfoResponseDto.class);

            // 헤더 코드 체크 (00: 정상)
            if (responseDto == null || responseDto.getHeader() == null) {
                log.warn("[API 경고] Receipt 응답 매핑 실패 또는 header 없음 - billId: {}", billId);
                return Optional.empty();
            }
            String code = responseDto.getHeader().getResultCode();
            if (code != null && !"00".equals(code)) {
                log.warn("[API 경고] Receipt resultCode != 00 (code={}, msg={} - billId: {}", code, responseDto.getHeader().getResultMsg(), billId);
                return Optional.empty();
            }

            return Optional.of(responseDto);
        } catch (Exception e) {
            log.error("[API 오류] getBillReceiptInfo 호출 실패: {}", billId, e);
            throw new RuntimeException("getBillReceiptInfo 호출 실패: " + billId);
        }
    }

    // TODO: 다른 API도 같은 방식으로 추가 가능
    public Optional<BillCommissionResponseDto> getBillCommissionInfo(String billId) {
        String url = API_BASE_URL + "getBillCommissionExaminationInfo"
                + "?serviceKey=" + serviceKey
                + "&bill_id=" + billId;

        URI uri = URI.create(url);

        try {
            String xml = restTemplate.getForObject(URI.create(url), String.class);
            return Optional.ofNullable(xmlMapper.readValue(xml, BillCommissionResponseDto.class));
        } catch (Exception e) {
            throw new RuntimeException("[API 오류] 위원회 정보 조회 실패 - billId: {}, error: {}" + billId, e);
        }
    }

    public Optional<BillDeliverateInfoResponseDto> getBillDeliverateInfo(String billId) {
        String url = API_BASE_URL + "getBillDeliverateInfo"
                + "?serviceKey=" + serviceKey
                + "&bill_id=" + billId;

        try {
            String xml = restTemplate.getForObject(URI.create(url), String.class);
            return Optional.of(xmlMapper.readValue(xml, BillDeliverateInfoResponseDto.class));
        } catch (Exception e) {
            log.warn("❌ 본회의 심의 정보 조회 실패 - billId: {}", billId, e);
            return Optional.empty();
        }
    }

    public Optional<BillTransferredInfoResponseDto> getBillTransferredInfo(String billId) {
        String url = API_BASE_URL + "getBillTransferredInfo"
                + "?serviceKey=" + serviceKey
                + "&bill_id=" + billId;

        try {
            String xml = restTemplate.getForObject(URI.create(url), String.class);
            BillTransferredInfoResponseDto dto = xmlMapper.readValue(xml, BillTransferredInfoResponseDto.class);
            return Optional.of(dto);
        } catch (Exception e) {
            log.warn("❌ 정부 이송 정보 조회 실패 - billId: {}", billId, e);
            return Optional.empty();
        }
    }

    public Optional<BillPromulgationInfoResponseDto> getBillPromulgationInfo(String billId) {
        String url = API_BASE_URL + "getBillPromulgationInfo"
                + "?serviceKey=" + serviceKey
                + "&bill_id=" + billId;

        URI uri = URI.create(url);

        try {
            String xml = restTemplate.getForObject(uri, String.class);
            BillPromulgationInfoResponseDto response = xmlMapper.readValue(xml, BillPromulgationInfoResponseDto.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.warn("❌ 공포 정보 조회 실패 - billId: {}", billId, e);
            return Optional.empty();
        }
    }

    public Optional<BillVoteResultResponseDto> getBillVoteResultInfo(String billId) {
        String url = API_BASE_URL_V2
                + "?KEY=" + serviceKeyV2
                + "&Type=xml&pIndex=1&pSize=1&AGE=22"
                + "&BILL_ID=" + billId;

        URI uri = URI.create(url);

        try {
            String xml = restTemplate.getForObject(URI.create(url), String.class);
            log.warn("응답 XML:\n{}", xml);

            // 빠르게 판별: <RESULT>로 시작하면 오류 응답
            if (xml.contains("<RESULT>") && !xml.contains("<row>")) {
                ResultOnlyResponseDto result = xmlMapper.readValue(xml, ResultOnlyResponseDto.class);
                if ("INFO-200".equals(result.getCode())) {
                    log.info("📭 표결 데이터 없음 (INFO-200) - billId: {}", billId);
                    return Optional.empty();
                } else {
                    log.warn("⚠️ 예기치 않은 코드: {} - billId: {}", result.getCode(), billId);
                    return Optional.empty();
                }
            }

            // 정상 응답 처리
            BillVoteResultResponseDto response = xmlMapper.readValue(xml, BillVoteResultResponseDto.class);
            return Optional.of(response);

        } catch (Exception e) {
            log.warn("❌ 표결 결과 조회 실패 - billId: {}", billId, e);
            return Optional.empty();
        }
    }

    /**
     * 최근 통과된 법안 목록 중 오늘 날짜에 처리된 법안만 조회
     * 
     * 이 메소드는 국회 공공데이터포털의 getRecentPasageList API를 호출하여
     * 최근 통과된 법안 목록을 가져온 후, 오늘 날짜(procDt)에 처리된 법안만 필터링하여 반환합니다.
     * 
     * API 엔드포인트: /getRecentPasageList (주의: API 이름에 오타가 있음 - Passage가 아닌 Pasage)
     * 
     * 페이징 처리:
     * - 첫 페이지(100건)가 모두 오늘 날짜인 경우, 다음 페이지도 확인
     * - 오늘이 아닌 날짜의 법안이 나올 때까지 페이징 진행
     * - numOfRows: 100 (페이지당 100건 조회)
     * 
     * 반환되는 법안 정보:
     * - billId: 법안 ID
     * - billName: 법안명
     * - billNo: 법안 번호
     * - committeeName: 위원회명
     * - generalResult: 처리결과 (예: 원안가결, 수정가결 등)
     * - procDt: 처리일자 (yyyy-MM-dd 형식)
     * - proposeDt: 제안일자
     * - proposerKind: 제안자 구분 (예: 의장, 위원장 등)
     * 
     * @return 통과 법안 조회 결과 (전체 조회 건수와 오늘 날짜 필터링된 목록 포함)
     * @throws RuntimeException API 호출 실패 시
     */
    public BillPassageResult getRecentPassageList() {
        List<BillRecentPassageResponseDto.ItemDto> allTodayPassedBills = new ArrayList<>();
        int pageNo = 1;
        int numOfRows = 100;
        int totalFetchedCount = 0;
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        boolean shouldContinue = true;
        
        while (shouldContinue) {
            String url = API_BASE_URL + "getRecentPasageList"
                    + "?serviceKey=" + serviceKey
                    + "&numOfRows=" + numOfRows
                    + "&pageNo=" + pageNo;

            URI uri = URI.create(url);

            log.info("[DEBUG] 최근 통과 법안 조회 - pageNo: {}, numOfRows: {}", pageNo, numOfRows);
            log.info(">> 최종 요청 URI: {}", url);

            try {
                String xml = restTemplate.getForObject(uri, String.class);
                log.debug("[DEBUG] XML 응답:\n{}", xml);
                BillRecentPassageResponseDto responseDto = xmlMapper.readValue(xml, BillRecentPassageResponseDto.class);
                
                if (responseDto.getBody() == null || responseDto.getBody().getItems() == null || responseDto.getBody().getItems().isEmpty()) {
                    log.info("[API 정보] 페이지 {}에 더 이상 데이터가 없습니다.", pageNo);
                    break;
                }
                
                List<BillRecentPassageResponseDto.ItemDto> items = responseDto.getBody().getItems();
                totalFetchedCount += items.size();
                
                // 현재 페이지에서 오늘 날짜의 법안만 필터링
                List<BillRecentPassageResponseDto.ItemDto> todayBillsInPage = items.stream()
                        .filter(item -> today.equals(item.getProcDt()))
                        .collect(Collectors.toList());
                
                allTodayPassedBills.addAll(todayBillsInPage);
                
                // 다음 페이지 확인 여부 결정
                // 1. 현재 페이지가 100건 미만이면 더 이상 데이터가 없음
                // 2. 현재 페이지의 모든 항목이 오늘 날짜가 아니면 중단
                // 3. 현재 페이지에 오늘이 아닌 날짜가 하나라도 있으면 중단 (이미 과거 날짜에 도달)
                boolean hasNonTodayBill = items.stream().anyMatch(item -> !today.equals(item.getProcDt()));
                
                if (items.size() < numOfRows || hasNonTodayBill) {
                    shouldContinue = false;
                    log.info("[DEBUG] 페이지 {} - 전체: {}건, 오늘: {}건 (종료 조건 만족)", 
                            pageNo, items.size(), todayBillsInPage.size());
                } else {
                    // 모든 항목이 오늘 날짜인 경우 다음 페이지 확인
                    pageNo++;
                    log.info("[DEBUG] 페이지의 모든 법안이 오늘 날짜입니다. 다음 페이지를 확인합니다.");
                }
                
            } catch (Exception e) {
                log.error("[API 오류] 최근 통과 법안 조회 실패 (pageNo: {}) - error: {}", pageNo, e.getMessage());
                throw new RuntimeException("getRecentPassageList 호출 실패");
            }
        }
        
        log.info("[DEBUG] 최종 결과: 전체 조회 {}건 중 오늘 통과된 법안 {}건", totalFetchedCount, allTodayPassedBills.size());
        
        return BillPassageResult.builder()
                .totalFetchedCount(totalFetchedCount)
                .todayPassedCount(allTodayPassedBills.size())
                .todayPassedBills(allTodayPassedBills)
                .build();
    }
}