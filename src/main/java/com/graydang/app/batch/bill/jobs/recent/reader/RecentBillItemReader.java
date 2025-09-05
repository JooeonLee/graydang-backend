package com.graydang.app.batch.bill.jobs.recent.reader;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillRecentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
@RequiredArgsConstructor
public class RecentBillItemReader implements ItemStreamReader<BillRecentResponseDto.ItemDto> {

    private final BillApiClient billApiClient;

    @Value("#{jobParameters['startPage'] ?: 1}")
    private int startPage;
    @Value("#{jobParameters['pageSize'] ?: 1}")
    private int pageSize;
    @Value("#{jobParameters['resume'] ?: false}")
    private boolean resume;

    private int pageNo;
    private int cursor = 0;
    private List<BillRecentResponseDto.ItemDto> buffer = List.of();
    private boolean finished = false;
    private boolean noMorePages = false;

    private static final String KEY_PAGE_NO = "recent.pageNo";
    private static final String KEY_CURSOR = "recent.cursor";

    @Override
    public BillRecentResponseDto.ItemDto read() {
        if (finished)
            return null;

        while (cursor >= buffer.size()) {
            if (noMorePages) {
                finished = true;
                return null;
            }

            // 이번에 읽을 페이지 호출 (항상 현재 pageNo 사용)
            List<BillRecentResponseDto.ItemDto> page = billApiClient.getBillRecentList(pageSize, pageNo);

            if (page == null || page.isEmpty()) {
                finished = true;
                return null;
            }

            buffer = page;
            cursor = 0;

            // 이번 페이지가  pageSize 보다 작으면  다음은 필요 없음(불필요 호출 방지)
            if(page.size() < pageSize)
                noMorePages = true;

            // 성공적으로 적재한 뒤 증가
            pageNo++;
        }

        return buffer.get(cursor++);
    }

    @Override
    public void open(ExecutionContext ctx) {
        if(resume) {
            // 재시작 모드에서만 복원
            this.pageNo = ctx.containsKey(KEY_PAGE_NO) ? ctx.getInt(KEY_PAGE_NO) : startPage;
            this.cursor = ctx.containsKey(KEY_CURSOR)  ? ctx.getInt(KEY_CURSOR)  : 0;
        } else {
            // 항상 처음부터
            this.pageNo = startPage;
            this.cursor = 0;
        }
    }

    @Override
    public void update(ExecutionContext ctx) {
        if (resume) {
            // 재시작 모드에서만 진행상황 저장
            ctx.putInt(KEY_PAGE_NO, pageNo);
            ctx.putInt(KEY_CURSOR,  cursor);
        }
    }

    @Override
    public void close() {}
}
