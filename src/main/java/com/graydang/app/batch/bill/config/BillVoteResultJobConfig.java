package com.graydang.app.batch.bill.config;

import com.graydang.app.batch.bill.tasklet.BillVoteResultProcessingTasklet;
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
public class BillVoteResultJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BillVoteResultProcessingTasklet billVoteResultProcessingTasklet;

    @Bean
    public Job billVoteResultJob() {
        return new JobBuilder("billVoteResultJob", jobRepository)
                .start(billVoteResultStep())
                .build();
    }

    @Bean
    public Step billVoteResultStep() {
        return new StepBuilder("billVoteResultStep", jobRepository)
                .tasklet(billVoteResultProcessingTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
