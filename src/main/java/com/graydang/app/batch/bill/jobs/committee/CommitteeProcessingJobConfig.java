package com.graydang.app.batch.bill.jobs.committee;

import com.graydang.app.batch.bill.jobs.committee.dto.BillUpdateDto;
import com.graydang.app.batch.bill.jobs.committee.processor.BillCommitteeItemProcessor;
import com.graydang.app.batch.bill.jobs.committee.writer.BillCommitteeItemWriter;
import com.graydang.app.batch.listener.CustomItemProcessListener;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class CommitteeProcessingJobConfig {

    // Reader에서 사용할 EntityManagerFactory
    private final EntityManagerFactory entityManagerFactory;

    private final BillCommitteeItemProcessor billCommitteeItemProcessor;
    private final BillCommitteeItemWriter billCommitteeItemWriter;

    private final CustomItemProcessListener customItemProcessListener;

    private static final int CHUNK_SIZE = 100;
    private static final int THREAD_POOL_SIZE = 10;

    // 1. Job 정의
    @Bean
    public Job billCommitteeJob(JobRepository jobRepository, Step billCommitteeUpdateStep) {

        return new JobBuilder("billCommitteeJob", jobRepository)
                .start(billCommitteeUpdateStep)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    // 2. Step 정의 (멀티스레드 Chunk 기반)
    @Bean
    public Step billCommitteeUpdateStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("billCommitteeUpdateStep", jobRepository)
                .<String, BillUpdateDto>chunk(CHUNK_SIZE, transactionManager) // <Reader 반환값, Writer 입력값>
                .reader(billIdReader())
                .processor(billCommitteeItemProcessor)
                .writer(billCommitteeItemWriter)
                .taskExecutor(taskExecutor()) // 멀티스레드 실행 설정
                .listener(customItemProcessListener)
                .build();
    }

    // 3. ItemReader 정의 (DB에서 billId 읽기)
    @Bean
    public JpaPagingItemReader<String> billIdReader() {
        return new JpaPagingItemReaderBuilder<String>()
                .name("billIdReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT b.billId FROM Bill b ORDER BY b.proposeDate DESC")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    // 4. TaskExecutor 정의 (스레드 풀)
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(THREAD_POOL_SIZE);
        executor.setMaxPoolSize(THREAD_POOL_SIZE);
        executor.setThreadNamePrefix("batch-thread-");
        executor.initialize();
        return executor;
    }
}
