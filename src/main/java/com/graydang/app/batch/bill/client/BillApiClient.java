package com.graydang.app.batch.bill.client;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.graydang.app.batch.bill.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

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

    public String getBillReceiptInfoXml(String billId) {
        String url = API_BASE_URL + "getBillReceiptInfo"
                + "?serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8)
                + "&billId=" + billId;

        try {
            return restTemplate.getForObject(url, String.class);
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
}