package com.graydang.app.batch.bill.config;

import com.graydang.app.batch.bill.tasklet.BillPlenaryProcessingTasklet;
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
public class BillPlenaryProcessingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BillPlenaryProcessingTasklet billPlenaryProcessingTasklet;

    @Bean
    public Job billPlenaryProcessingJob() {
        return new JobBuilder("billPlenaryProcessingJob", jobRepository)
                .start(billPlenaryProcessingStep())
                .build();
    }

    @Bean
    public Step billPlenaryProcessingStep() {
        return new StepBuilder("billPlenaryProcessingStep", jobRepository)
                .tasklet(billPlenaryProcessingTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
