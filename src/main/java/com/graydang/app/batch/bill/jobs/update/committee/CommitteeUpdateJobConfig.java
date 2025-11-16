package com.graydang.app.batch.bill.jobs.update.committee;

import com.graydang.app.batch.bill.jobs.update.committee.dto.BillStatusHistoryCommitteeUpdateDto;
import com.graydang.app.batch.bill.jobs.update.committee.processor.BillStatusHistoryCommitteeItemProcessor;
import com.graydang.app.batch.bill.jobs.update.committee.writer.BillStatusHistoryCommitteeItemWriter;
import com.graydang.app.batch.bill.listener.BillStepLoggingListener;
import com.graydang.app.domain.bill.model.Bill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommitteeUpdateJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final DataSource dataSource; // JdbcPagingItemReader를 위해 DataSource 사용

    private final BillStatusHistoryCommitteeItemProcessor itemProcessor;
    private final BillStatusHistoryCommitteeItemWriter itemWriter;

    private final BillStepLoggingListener loggingListener;

    private static final int CHUNK_SIZE = 100;
    private static final int THREAD_POOL_SIZE = 10;

    // 1. Job 정의
    @Bean
    public Job billStatusHistoryCommitteeUpdateJob(Step billStatusHistoryCommitteeUpdateStep) {

        return new JobBuilder("billStatusHistoryCommitteeUpdateJob", jobRepository)
                .start(billStatusHistoryCommitteeUpdateStep)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    // 2. Step 정의 (멀티스레드 Chunk 기반)
    @Bean
    public Step billStatusHistoryCommitteeUpdateStep(
            JdbcPagingItemReader<Bill> committeeUpdateCheckItemReader,
            TaskExecutor committeeUpdateTaskExecutor) {
        return new StepBuilder("billStatusHistoryCommitteeUpdateStep", jobRepository)
                .<Bill, BillStatusHistoryCommitteeUpdateDto>chunk(CHUNK_SIZE, txManager)
                .reader(committeeUpdateCheckItemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .taskExecutor(committeeUpdateTaskExecutor)
                .listener(loggingListener)
                .build();
    }

    @Bean
    public JdbcPagingItemReader<Bill> committeeUpdateCheckItemReader() {
        String whereClause = "WHERE NOT EXISTS (SELECT 1 FROM bill_status_history h WHERE h.bill_id = b.id)"
                + " AND b.bill_id IS NOT NULL";

        String selectClause = "SELECT " +
                "id, " +
                "created_at AS createdAt, " +
                "updated_at AS updatedAt, " +
                "ai_summary AS aiSummary, " +
                "ai_title AS aiTitle, " +
                "bill_id AS billId, " +
                "bill_status AS billStatus, " +
                "committee_name AS committeeName, " +
                "process_result AS processResult, " +
                "propose_date AS proposeDate, " +
                "representative_name AS representativeName, " +
                "status, " +
                "summary, " +
                "title, " +
                "ai_processed AS aiProcessed, " +
                "view_count AS viewCount";

        return new JdbcPagingItemReaderBuilder<Bill>()
                .name("committeeUpdateCheckItemReader")
                .dataSource(dataSource)
                .selectClause(selectClause)
                .fromClause("FROM bill b")
                .whereClause(whereClause)
                .sortKeys(Map.of("id", Order.ASCENDING))
                .pageSize(CHUNK_SIZE)
                .rowMapper(new BeanPropertyRowMapper<>(Bill.class)) // snake_case -> camelCase 자동 매핑
                .build();
    }

    @Bean
    public TaskExecutor committeeUpdateTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(THREAD_POOL_SIZE);
        executor.setMaxPoolSize(THREAD_POOL_SIZE);
        executor.setThreadNamePrefix("batch-thread-");
        executor.initialize();
        return executor;
    }
}
