package com.graydang.app.batch.bill.jobs.update.committee;

import com.graydang.app.batch.bill.jobs.update.committee.dto.BillStatusHistoryCommitteeUpdateDto;
import com.graydang.app.batch.bill.jobs.update.committee.processor.BillStatusHistoryCommitteeCompletionItemProcessor;
import com.graydang.app.batch.bill.jobs.update.committee.writer.BillStatusHistoryCommitteeCompletionItemWriter;
import com.graydang.app.batch.bill.listener.BillStepLoggingListener;
import com.graydang.app.batch.bill.listener.JobCompletionNotificationListener;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommitteeUpdateCompletionJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final DataSource dataSource;

    // 리스너
    private final BillStepLoggingListener stepLoggingListener;
    private final JobCompletionNotificationListener jobLoggingListener;

    // Processor & Writer 주입
    private final BillStatusHistoryCommitteeCompletionItemProcessor itemProcessor;
    private final BillStatusHistoryCommitteeCompletionItemWriter itemWriter;

    private static final int CHUNK_SIZE = 100;
    private static final int THREAD_POOL_SIZE = 10;

    // 1. Job 정의
    @Bean
    public Job billStatusHistoryCommitteeCompletionJob(Step billStatusHistoryCommitteeCompletionStep) {
        return new JobBuilder("billStatusHistoryCommitteeCompletionJob", jobRepository)
                .start(billStatusHistoryCommitteeCompletionStep)
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener) // 슬랙 알림
                .build();
    }

    // 2. Step 정의
    @Bean
    public Step billStatusHistoryCommitteeCompletionStep(
            JdbcPagingItemReader<Bill> reviewCompletionReader,
            TaskExecutor committeeUpdateCompletionTaskExecutor) { // Executor는 기존 것 재사용 가능

        return new StepBuilder("committeeReviewCompletionStep", jobRepository)
                .<Bill, BillStatusHistoryCommitteeUpdateDto>chunk(CHUNK_SIZE, txManager)
                .reader(reviewCompletionReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .taskExecutor(committeeUpdateCompletionTaskExecutor)
                .listener(stepLoggingListener) // 로그 리스너
                .build();
    }

    // 3. Reader 정의
    @Bean
    public JdbcPagingItemReader<Bill> reviewCompletionReader() {

        // [쿼리 로직]
        // 1. bill(b)과 bill_status_history(h)를 JOIN
        // 2. h.step_name이 '소관위 회부' 이고
        // 3. h.step_result가 '심사 중' 인 데이터만 조회 (업데이트 대상)

        String selectClause = "SELECT " +
                "b.id, " + // b.id로 명시
                "b.created_at AS createdAt, " +
                "b.updated_at AS updatedAt, " +
                "b.ai_summary AS aiSummary, " +
                "b.ai_title AS aiTitle, " +
                "b.bill_id AS billId, " +
                "b.bill_status AS billStatus, " +
                "b.committee_name AS committeeName, " +
                "b.process_result AS processResult, " +
                "b.propose_date AS proposeDate, " +
                "b.representative_name AS representativeName, " +
                "b.status, " +
                "b.summary, " +
                "b.title, " +
                "b.ai_processed AS aiProcessed, " +
                "b.view_count AS viewCount";

        // INNER JOIN을 사용하여 history가 있는 데이터만 가져옴
        String fromClause = "FROM bill b INNER JOIN bill_status_history h ON b.id = h.bill_id";

        // '심사 중' 상태인 것만 필터링
        String whereClause = "WHERE h.step_name = '위원회 회부' AND h.step_result = '심사 중'";

        return new JdbcPagingItemReaderBuilder<Bill>()
                .name("reviewCompletionReader")
                .dataSource(dataSource)
                .selectClause(selectClause)
                .fromClause(fromClause)
                .whereClause(whereClause)
                .sortKeys(Map.of("b.id", Order.ASCENDING)) // b.id 기준 정렬 (Select절의 별칭 id 사용)
                .pageSize(CHUNK_SIZE)
                // (4) @Setter가 없는 엔티티라면 수동 매핑 필요 (이전과 동일)
                .rowMapper((rs, rowNum) -> {
                    java.sql.Date proposeDateSql = rs.getDate("proposeDate");
                    java.time.LocalDate proposeDate = (proposeDateSql != null) ? proposeDateSql.toLocalDate() : null;

                    return Bill.builder()
                            .id(rs.getLong("id"))
                            .aiSummary(rs.getString("aiSummary"))
                            .aiTitle(rs.getString("aiTitle"))
                            .billId(rs.getString("billId"))
                            .billStatus(rs.getString("billStatus"))
                            .committeeName(rs.getString("committeeName"))
                            .processResult(rs.getString("processResult"))
                            .proposeDate(proposeDate)
                            .representativeName(rs.getString("representativeName"))
                            .status(rs.getString("status"))
                            .summary(rs.getString("summary"))
                            .title(rs.getString("title"))
                            .aiProcessed(rs.getBoolean("aiProcessed"))
                            .viewCount(rs.getLong("viewCount"))
                            .build();
                })
                .build();
    }

    @Bean
    public TaskExecutor committeeUpdateCompletionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(THREAD_POOL_SIZE);
        executor.setMaxPoolSize(THREAD_POOL_SIZE);
        executor.setThreadNamePrefix("batch-thread-");
        executor.initialize();
        return executor;
    }
}
