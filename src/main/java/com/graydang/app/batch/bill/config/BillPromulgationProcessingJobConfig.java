package com.graydang.app.batch.bill.config;

import com.graydang.app.batch.bill.tasklet.BillPromulgationProcessingTasklet;
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
public class BillPromulgationProcessingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BillPromulgationProcessingTasklet billPromulgationProcessingTasklet;

    @Bean
    public Job billPromulgationProcessingJob() {
        return new JobBuilder("billPromulgationProcessingJob", jobRepository)
                .start(billPromulgationProcessingStep())
                .build();
    }

    @Bean
    public Step billPromulgationProcessingStep() {
        return new StepBuilder("billPromulgationProcessingStep", jobRepository)
                .tasklet(billPromulgationProcessingTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
