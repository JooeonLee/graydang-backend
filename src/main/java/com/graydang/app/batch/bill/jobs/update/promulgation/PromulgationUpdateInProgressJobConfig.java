package com.graydang.app.batch.bill.jobs.update.promulgation;

import com.graydang.app.batch.bill.jobs.update.promulgation.dto.BillStatusHistoryPromulgationUpdateDto;
import com.graydang.app.batch.bill.jobs.update.promulgation.processor.BillStatusHistoryPromulgationItemProcessor;
import com.graydang.app.batch.bill.jobs.update.promulgation.writer.BillStatusHistoryPromulgationItemWriter;
import com.graydang.app.batch.bill.jobs.update.transfer.dto.BillStatusHistoryGovTransferUpdateDto;
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
public class PromulgationUpdateInProgressJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final EntityManagerFactory entityManagerFactory;

    private final BillStatusHistoryPromulgationItemProcessor delegateProcessor;
    private final BillStatusHistoryPromulgationItemWriter delegateWriter;

    private final BillStepLoggingListener stepLoggingListener;
    private final JobCompletionNotificationListener jobLoggingListener;

    private static final int CHUNK_SIZE = 100;
    private static final int API_THREAD_SIZE = 10;
    private static final int API_MAX_THREAD_SIZE = 20;

    // 1. Async Job 정의
    @Bean
    public Job billStatusHistoryPromulgationInProgressAsyncJob() {
        return new JobBuilder("billStatusHistoryPromulgationInProgressAsyncJob", jobRepository)
                .start(billStatusHistoryPromulgationInProgressAsyncStep())
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .build();
    }

    // 2. Async Step 정의
    @Bean
    public Step billStatusHistoryPromulgationInProgressAsyncStep() {
        return new StepBuilder("billStatusHistoryPromulgationInProgressAsyncStep", jobRepository)
                // [핵심] Output 타입: Future<?>
                .<Bill, Future<BillStatusHistoryPromulgationUpdateDto>>chunk(CHUNK_SIZE, txManager)
                .reader(promulgationInProgressAsyncReader())      // Main Thread (Single)
                .processor(promulgationInProgressAsyncProcessor()) // Worker Threads (Parallel)
                .writer(promulgationInProgressAsyncWriter())       // Main Thread (Single)
                // [주의] .taskExecutor() 제거! (Reader 동기화 문제 해결)
                .listener(stepLoggingListener)
                .listener(delegateProcessor)
                .build();
    }

    // 3. Reader: JPA 기반
    @Bean
    public JpaCursorItemReader<Bill> promulgationInProgressAsyncReader() {
        return new JpaCursorItemReaderBuilder<Bill>()
                .name("promulgationInProgressAsyncReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT DISTINCT b FROM Bill b " +
                        "JOIN BillStatusHistory h ON h.bill = b " +
                        "WHERE h.stepName = '정부 이송' " +
                        "AND h.stepResult <> '심사 중' " +
                        "AND NOT EXISTS (" +
                        "   SELECT subH FROM BillStatusHistory subH " +
                        "   WHERE subH.bill = b " +
                        "   AND subH.stepName = '공포' " +
                        ") " +
                        "ORDER BY b.id ASC")
                .build();
    }

    @Bean
    public AsyncItemProcessor<Bill, BillStatusHistoryPromulgationUpdateDto> promulgationInProgressAsyncProcessor() {
        AsyncItemProcessor<Bill, BillStatusHistoryPromulgationUpdateDto> processor = new AsyncItemProcessor<>();
        processor.setDelegate(delegateProcessor);
        processor.setTaskExecutor(promulgationAsyncApiTaskExecutor());
        return processor;
    }

    @Bean
    public AsyncItemWriter<BillStatusHistoryPromulgationUpdateDto> promulgationInProgressAsyncWriter() {
        AsyncItemWriter<BillStatusHistoryPromulgationUpdateDto> writer = new AsyncItemWriter<>();
        writer.setDelegate(delegateWriter);
        return writer;
    }

    @Bean
    public TaskExecutor promulgationAsyncApiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(API_THREAD_SIZE);
        executor.setMaxPoolSize(API_MAX_THREAD_SIZE);
        executor.setThreadNamePrefix("async-promulgation-thread-");
        executor.initialize();
        return executor;
    }
}
