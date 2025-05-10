package com.graydang.app.batch.bill.client;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;
import com.graydang.app.batch.bill.dto.BillDeliverateInfoResponseDto;
import com.graydang.app.batch.bill.dto.BillInfoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

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

    private final String API_BASE_URL = "https://apis.data.go.kr/9710000/BillInfoService2/";

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
//            log.info("[DEBUG] REQUEST URL:\n{}", uri);
//            log.info("[DEBUG] XML <UNK>:\n{}", xml);
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

}