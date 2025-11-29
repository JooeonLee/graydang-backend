package com.graydang.app.batch.bill.jobs.update.committee;

import com.graydang.app.batch.bill.jobs.update.committee.dto.BillStatusHistoryCommitteeUpdateDto;
import com.graydang.app.batch.bill.jobs.update.committee.processor.BillStatusHistoryCommitteeCompletionItemProcessor;
import com.graydang.app.batch.bill.jobs.update.committee.writer.BillStatusHistoryCommitteeCompletionItemWriter;
import com.graydang.app.batch.bill.listener.BillStepLoggingListener;
import com.graydang.app.batch.bill.listener.JobCompletionNotificationListener;
import com.graydang.app.domain.bill.model.Bill;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.Future;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommitteeUpdateCompletionAsyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final EntityManagerFactory entityManagerFactory; // JPA용

    // 리스너
    private final BillStepLoggingListener stepLoggingListener;
    private final JobCompletionNotificationListener jobLoggingListener;

    // 기존 로직(Processor & Writer) 주입
    private final BillStatusHistoryCommitteeCompletionItemProcessor delegateProcessor;
    private final BillStatusHistoryCommitteeCompletionItemWriter delegateWriter;

    private static final int CHUNK_SIZE = 100;
    private static final int API_THREAD_SIZE = 10;
    private static final int API_MAX_THREAD_SIZE = 20;

    // 1. Async Job 정의
    @Bean
    public Job billStatusHistoryCommitteeCompletionAsyncJob() {
        return new JobBuilder("billStatusHistoryCommitteeCompletionAsyncJob", jobRepository)
                .start(billStatusHistoryCommitteeCompletionAsyncStep())
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .build();
    }

    // 2. Async Step 정의
    @Bean
    public Step billStatusHistoryCommitteeCompletionAsyncStep() {
        return new StepBuilder("billStatusHistoryCommitteeCompletionAsyncStep", jobRepository)
                // [핵심] Output 타입: Future<?>
                .<Bill, Future<BillStatusHistoryCommitteeUpdateDto>>chunk(CHUNK_SIZE, txManager)
                .reader(completionAsyncReader())      // Main Thread (Single)
                .processor(completionAsyncProcessor()) // Worker Threads (Parallel)
                .writer(completionAsyncWriter())       // Main Thread (Single)
                // [주의] .taskExecutor() 제거! (Reader 동기화 문제 해결)
                .listener(stepLoggingListener)
                .listener(delegateProcessor)
                .build();
    }

    // 3. Reader: JPA 기반으로 변경 (JPQL 사용)
    @Bean
    public JpaPagingItemReader<Bill> completionAsyncReader() {
        return new JpaPagingItemReaderBuilder<Bill>()
                .name("completionAsyncReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                // [JPQL 작성]
                // 기존 SQL: FROM bill b INNER JOIN bill_status_history h ON b.id = h.bill_id
                // 기존 조건: WHERE h.step_name = '위원회 회부' AND h.step_result = '심사 중'
                //
                // [변환 포인트]
                // 1. BillStatusHistory(h)와 Bill(b)을 조인합니다.
                // 2. h.bill = b (연관관계 필드 비교)
                // 3. ORDER BY b.id ASC (필수!)
                .queryString("SELECT b FROM Bill b, BillStatusHistory h " +
                        "WHERE h.bill = b " +
                        "AND h.stepName = '위원회 회부' " +
                        "AND h.stepResult = '심사 중' " +
                        "ORDER BY b.id ASC")
                .build();
    }

    // 4. Processor: Async 래퍼 적용
    @Bean
    public AsyncItemProcessor<Bill, BillStatusHistoryCommitteeUpdateDto> completionAsyncProcessor() {
        AsyncItemProcessor<Bill, BillStatusHistoryCommitteeUpdateDto> processor = new AsyncItemProcessor<>();
        processor.setDelegate(delegateProcessor);
        processor.setTaskExecutor(completionAsyncApiTaskExecutor());
        return processor;
    }

    // 5. Writer: Async 래퍼 적용
    @Bean
    public AsyncItemWriter<BillStatusHistoryCommitteeUpdateDto> completionAsyncWriter() {
        AsyncItemWriter<BillStatusHistoryCommitteeUpdateDto> writer = new AsyncItemWriter<>();
        writer.setDelegate(delegateWriter);
        return writer;
    }

    // 6. API 호출용 스레드 풀
    @Bean
    public TaskExecutor completionAsyncApiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(API_THREAD_SIZE);
        executor.setMaxPoolSize(API_MAX_THREAD_SIZE);
        executor.setThreadNamePrefix("async-compl-thread-");
        executor.initialize();
        return executor;
    }
}
