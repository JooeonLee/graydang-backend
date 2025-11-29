package com.graydang.app.batch.bill.jobs.update.committee;

import com.graydang.app.batch.bill.jobs.update.committee.dto.BillStatusHistoryCommitteeUpdateDto;
import com.graydang.app.batch.bill.jobs.update.committee.processor.BillStatusHistoryCommitteeItemProcessor;
import com.graydang.app.batch.bill.jobs.update.committee.writer.BillStatusHistoryCommitteeItemWriter;
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
public class CommitteeUpdateInProgressJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final EntityManagerFactory entityManagerFactory;

    private final BillStatusHistoryCommitteeItemProcessor delegateProcessor;
    private final BillStatusHistoryCommitteeItemWriter delegateWriter;

    private final BillStepLoggingListener stepLoggingListener;
    private final JobCompletionNotificationListener jobLoggingListener;

    private static final int CHUNK_SIZE = 100;
    private static final int API_THREAD_SIZE = 10;
    private static final int API_MAX_THREAD_SIZE = 20;

    // 1. Async Job 정의
    @Bean
    public Job billStatusHistoryCommitteeUpdateAsyncJob() {
        return new JobBuilder("billStatusHistoryCommitteeUpdateAsyncJob", jobRepository)
                .start(billStatusHistoryCommitteeUpdateAsyncStep())
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .build();

    }

    // 2. Async Step 정의
    @Bean
    public Step billStatusHistoryCommitteeUpdateAsyncStep() {
        return new StepBuilder("billStatusHistoryCommitteeUpdateAsyncStep", jobRepository)
                // [중요] Output 타입이 Future<?>로 감싸집니다.
                .<Bill, Future<BillStatusHistoryCommitteeUpdateDto>>chunk(CHUNK_SIZE, txManager)
                .reader(committeeUpdateAsyncItemReader()) // Reader는 단일 스레드 (DB 안정성 확보)
                .processor(asyncItemProcessor())        // 비동기 프로세서
                .writer(asyncItemWriter())              // 비동기 라이터
                // 주의: 여기에 .taskExecutor()를 붙이면 안 됩니다! (Step 자체는 단일 스레드여야 함)
                .listener(stepLoggingListener)
                .listener(delegateProcessor)
                .build();
    }


    @Bean
    public JpaPagingItemReader<Bill> committeeUpdateAsyncItemReader() {
        return new JpaPagingItemReaderBuilder<Bill>()
                .name("committeeUpdateAsyncItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                // [JPQL 작성]
                // Native Query가 아니라 Entity 기반의 JPQL을 작성합니다.
                // Bill 엔티티와 BillStatusHistory 엔티티의 연관관계가 없다면,
                // 아래처럼 서브쿼리나 JOIN을 JPQL 문법에 맞춰 작성해야 합니다.
                .queryString("SELECT b FROM Bill b " +
                        "WHERE b.billId IS NOT NULL " +
                        "AND NOT EXISTS (SELECT h.id FROM BillStatusHistory h WHERE h.bill = b) " +
                        "ORDER BY b.id ASC")
                .build();
    }

    // 3. AsyncItemProcessor (기존 Processor를 감싸서 비동기로 실행)
    @Bean
    public AsyncItemProcessor<Bill, BillStatusHistoryCommitteeUpdateDto> asyncItemProcessor() {
        AsyncItemProcessor<Bill, BillStatusHistoryCommitteeUpdateDto> processor = new AsyncItemProcessor<>();

        processor.setDelegate(delegateProcessor); // 실제 로직(API 호출)을 가진 객체
        processor.setTaskExecutor(asyncApiTaskExecutor()); // API 호출을 담당할 스레드 풀

        return processor;
    }

    // 4. AsyncItemWriter (기존 Processor를 감싸서 비동기로 실행)
    @Bean
    public AsyncItemWriter<BillStatusHistoryCommitteeUpdateDto> asyncItemWriter() {
        AsyncItemWriter<BillStatusHistoryCommitteeUpdateDto> writer = new AsyncItemWriter<>();

        writer.setDelegate(delegateWriter); // 실제 DB 저장을 담당할 객체

        return writer;
    }

    // 5. API 호출 전용 스레드 풀
    @Bean
    public TaskExecutor asyncApiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(API_THREAD_SIZE);
        executor.setMaxPoolSize(API_MAX_THREAD_SIZE);
        executor.setThreadNamePrefix("async-api-thread-");
        executor.initialize();
        return executor;
    }
}
