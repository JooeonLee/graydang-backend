package com.graydang.app.batch.bill.config;

import com.graydang.app.batch.bill.tasklet.BillGptSummaryTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BillGptSummaryJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BillGptSummaryTasklet billGptSummaryTasklet;

    @Bean
    public Job billGptSummaryJob() {
        return new JobBuilder("billGptSummaryJob", jobRepository)
                .start(billGptSummaryStep())
                .build();
    }

    @Bean
    public Step billGptSummaryStep() {
        return new StepBuilder("billGptSummaryStep", jobRepository)
                .tasklet(billGptSummaryTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
