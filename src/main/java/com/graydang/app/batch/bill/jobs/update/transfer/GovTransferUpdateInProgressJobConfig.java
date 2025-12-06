package com.graydang.app.batch.bill.jobs.update.transfer;

import com.graydang.app.batch.bill.jobs.update.plenary.dto.BillStatusHistoryPlenaryUpdateDto;
import com.graydang.app.batch.bill.jobs.update.transfer.dto.BillStatusHistoryGovTransferUpdateDto;
import com.graydang.app.batch.bill.jobs.update.transfer.processor.BillStatusHistoryGovTransferItemProcessor;
import com.graydang.app.batch.bill.jobs.update.transfer.writer.BillStatusHistoryGovTransferItemWriter;
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
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.Future;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GovTransferUpdateInProgressJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final EntityManagerFactory entityManagerFactory;

    private final BillStatusHistoryGovTransferItemProcessor delegateProcessor;
    private final BillStatusHistoryGovTransferItemWriter delegateWriter;

    private final BillStepLoggingListener stepLoggingListener;
    private final JobCompletionNotificationListener jobLoggingListener;

    private static final int CHUNK_SIZE = 100;
    private static final int API_THREAD_SIZE = 10;
    private static final int API_MAX_THREAD_SIZE = 20;

    // 1. Async Job 정의
    @Bean
    public Job billStatusHistoryGovTransferInProgressAsyncJob() {
        return new JobBuilder("billStatusHistoryGovTransferInProgressAsyncJob", jobRepository)
                .start(billStatusHistoryGovTransferInProgressAsyncStep())
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .build();
    }

    // 2. Async Step 정의
    @Bean
    public Step billStatusHistoryGovTransferInProgressAsyncStep() {
        return new StepBuilder("billStatusHistoryGovTransferInProgressAsyncStep", jobRepository)
                // [핵심] Output 타입: Future<?>
                .<Bill, Future<BillStatusHistoryGovTransferUpdateDto>>chunk(CHUNK_SIZE, txManager)
                .reader(govTransferInProgressAsyncReader())      // Main Thread (Single)
                .processor(govTransferInProgressAsyncProcessor()) // Worker Threads (Parallel)
                .writer(govTransferInProgressAsyncWriter())       // Main Thread (Single)
                // [주의] .taskExecutor() 제거! (Reader 동기화 문제 해결)
                .listener(stepLoggingListener)
                .listener(delegateProcessor)
                .build();
    }

    // 3. Reader: JPA 기반
    @Bean
    public JpaCursorItemReader<Bill> govTransferInProgressAsyncReader() {
        return new JpaCursorItemReaderBuilder<Bill>()
                .name("plenaryInProgressAsyncReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT b FROM Bill b " +
                        "JOIN BillStatusHistory h ON h.bill = b " +
                        "WHERE h.stepName = '본회의 심의' " +
                        "AND h.stepResult <> '심사 중' " +
                        "AND NOT EXISTS (" +
                        "   SELECT subH FROM BillStatusHistory subH " +
                        "   WHERE subH.bill = b " +
                        "   AND subH.stepName = '정부 이송' " +
                        ") " +
                        "ORDER BY b.id ASC")
                .build();
    }

    @Bean
    public AsyncItemProcessor<Bill, BillStatusHistoryGovTransferUpdateDto> govTransferInProgressAsyncProcessor() {
        AsyncItemProcessor<Bill, BillStatusHistoryGovTransferUpdateDto> processor = new AsyncItemProcessor<>();
        processor.setDelegate(delegateProcessor);
        processor.setTaskExecutor(govTransferAsyncApiTaskExecutor());
        return processor;
    }

    @Bean
    public AsyncItemWriter<BillStatusHistoryGovTransferUpdateDto> govTransferInProgressAsyncWriter() {
        AsyncItemWriter<BillStatusHistoryGovTransferUpdateDto> writer = new AsyncItemWriter<>();
        writer.setDelegate(delegateWriter);
        return writer;
    }

    @Bean
    public TaskExecutor govTransferAsyncApiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(API_THREAD_SIZE);
        executor.setMaxPoolSize(API_MAX_THREAD_SIZE);
        executor.setThreadNamePrefix("async-gov-transfer-thread-");
        executor.initialize();
        return executor;
    }
}
