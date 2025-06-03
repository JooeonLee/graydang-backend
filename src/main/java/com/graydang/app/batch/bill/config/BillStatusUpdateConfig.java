package com.graydang.app.batch.bill.config;

import com.graydang.app.batch.bill.tasklet.BillStatusUpdateTasklet;
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
public class BillStatusUpdateConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BillStatusUpdateTasklet billStatusUpdateTasklet;

    @Bean
    public Job billStatusUpdateJob() {
        return new JobBuilder("billStatusUpdateJob", jobRepository)
                .start(billStatusUpdateStep())
                .build();
    }

    @Bean
    public Step billStatusUpdateStep() {
        return new StepBuilder("billStatusUpdateStep", jobRepository)
                .tasklet(billStatusUpdateTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
