package com.graydang.app.batch.bill.config;

import com.graydang.app.batch.bill.tasklet.BillSyncTasklet;
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
public class BillSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BillSyncTasklet billSyncTasklet;

    @Bean
    public Job billSyncJob() {
        return new JobBuilder("billSyncJob", jobRepository)
                .start(billSyncStep())
                .build();
    }

    @Bean
    public Step billSyncStep() {
        return new StepBuilder("billSyncStep", jobRepository)
                .tasklet(billSyncTasklet, transactionManager)
                //.allowStartIfComplete(true)
                .build();
    }
}
