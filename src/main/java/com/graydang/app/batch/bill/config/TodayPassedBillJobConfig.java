package com.graydang.app.batch.bill.config;

import com.graydang.app.batch.bill.tasklet.TodayPassedBillTasklet;
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
public class TodayPassedBillJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TodayPassedBillTasklet todayPassedBillTasklet;

    @Bean
    public Job todayPassedBillJob() {
        return new JobBuilder("todayPassedBillJob", jobRepository)
                .start(todayPassedBillStep())
                .build();
    }

    @Bean
    public Step todayPassedBillStep() {
        return new StepBuilder("todayPassedBillStep", jobRepository)
                .tasklet(todayPassedBillTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
