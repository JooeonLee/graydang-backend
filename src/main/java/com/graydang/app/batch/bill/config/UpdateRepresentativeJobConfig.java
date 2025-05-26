package com.graydang.app.batch.bill.config;

import com.graydang.app.batch.bill.tasklet.UpdateRepresentativeTasklet;
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
public class UpdateRepresentativeJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UpdateRepresentativeTasklet updateRepresentativeTasklet;

    @Bean
    public Job updateRepresentativeJob() {
        return new JobBuilder("updateRepresentativeJob", jobRepository)
                .start(updateRepresentativeStep())
                .build();
    }

    @Bean
    public Step updateRepresentativeStep() {
        return new StepBuilder("updateRepresentativeStep", jobRepository)
                .tasklet(updateRepresentativeTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
