package com.graydang.app.batch.bill.config;

import com.graydang.app.batch.bill.tasklet.BillGovTransferredProcessingTasklet;
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
public class BillGovTransferredJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BillGovTransferredProcessingTasklet billGovTransferredProcessingTasklet;

    @Bean
    public Job billGovTransferredProcessingJob() {
        return new JobBuilder("billGovTransferredProcessingJob", jobRepository)
                .start(billGovTransferredProcessingStep())
                .build();
    }

    @Bean
    public Step billGovTransferredProcessingStep() {
        return new StepBuilder("billGovTransferredProcessingStep", jobRepository)
                .tasklet(billGovTransferredProcessingTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
