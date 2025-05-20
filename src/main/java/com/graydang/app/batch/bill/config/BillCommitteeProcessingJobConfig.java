package com.graydang.app.batch.bill.config;

import com.graydang.app.batch.bill.tasklet.BillCommitteeProcessingTasklet;
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
public class BillCommitteeProcessingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BillCommitteeProcessingTasklet billCommitteeProcessingTasklet;

    @Bean
    public Job billCommitteeProcessingJob() {
        return new JobBuilder("billCommitteeProcessingJob", jobRepository)
                .start(billCommitteeProcessingStep())
                .build();
    }

    @Bean
    public Step billCommitteeProcessingStep() {
        return new StepBuilder("billCommitteeProcessingStep", jobRepository)
                .tasklet(billCommitteeProcessingTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}