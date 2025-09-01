package com.graydang.app.batch.bill.jobs.recent;

import com.graydang.app.batch.bill.dto.BillRecentResponseDto;
import com.graydang.app.batch.bill.dto.BillSaveRequestDto;
import com.graydang.app.batch.bill.jobs.recent.processor.RecentBillItemProcessor;
import com.graydang.app.batch.bill.jobs.recent.reader.RecentBillItemReader;
import com.graydang.app.batch.bill.jobs.recent.writer.RecentBillItemWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class RecentBillJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;

    @Bean
    public Job collectRecentBillsJob(Step collectRecentBillsStep) {
        return new JobBuilder("collectRecentBillsJob", jobRepository)
                .incrementer(defaultParametersIncrementer())
                .start(collectRecentBillsStep)
                .build();
    }

    @Bean
    public Step collectRecentBillsStep(RecentBillItemReader reader,
                                       RecentBillItemProcessor processor,
                                       RecentBillItemWriter writer) {
        return new StepBuilder("collectRecentBillsStep", jobRepository)
                .<BillRecentResponseDto.ItemDto, BillSaveRequestDto>chunk(50, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .allowStartIfComplete(true)
                .faultTolerant()
                .skipLimit(100)
                .skip(Exception.class)
                .build();
    }

    @Bean
    public JobParametersIncrementer defaultParametersIncrementer() {
        return new JobParametersIncrementer() {
            @Override
            public JobParameters getNext(JobParameters jobParameters) {
                JobParametersBuilder builder = new JobParametersBuilder(
                        jobParameters == null ? new JobParameters() : jobParameters
                );
                // 기본값이 없을 때만 채움
                if(builder.toJobParameters().getString("startPage") == null) builder.addString("startPage", "1");
                if(builder.toJobParameters().getString("pageSize") == null) builder.addString("pageSize", "100");
                if(builder.toJobParameters().getString("resume") == null) builder.addString("resume", "false");

                // 매 실행마다 유니크 run.id 부여 (JobInstance 충돌 방지)
                builder.addLong("run.id", System.currentTimeMillis());

                return builder.toJobParameters();
            }
        };
    }

    @Bean
    public JobExecutionListener logJobParamsListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                System.out.println("[BATCH] Starting job " + jobExecution.getJobInstance().getJobName() +
                        " with params: " + jobExecution.getJobParameters());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                System.out.println("[BATCH] Finished job " + jobExecution.getJobInstance().getJobName() +
                        " with status: " + jobExecution.getStatus());
            }
        };
    }
}
