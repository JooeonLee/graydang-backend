package com.graydang.app;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillInfoResponseDto;
import com.graydang.app.domain.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BillInitialLoader implements CommandLineRunner {

    private final BillApiClient billApiClient;
    private final BillService billService;

    @Override
    public void run(String... args) throws Exception {
        int pageNo = 1;
        int numOfRows = 100;

        List<BillInfoResponseDto.ItemDto> items = billApiClient.getBillInfoList(numOfRows, pageNo);

        for (BillInfoResponseDto.ItemDto item : items) {
            billService.saveOrUpdate(item);
        }
    }
}
