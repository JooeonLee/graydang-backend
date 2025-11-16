package com.graydang.app.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchAsyncConfig {

    private final JobRepository jobRepository;

    @Bean
    public JobLauncher asyncJobLauncher() throws Exception{

        // (3) SimpleJobLauncher 대신 DefaultJobLauncher 사용
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        // (4) 비동기 실행을 위한 TaskExecutor 설정은 동일하게 적용
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor("batch-launcher-"));

        try {
            // (5) Bean 생명주기에 따라 afterPropertiesSet() 호출
            jobLauncher.afterPropertiesSet();
        } catch (Exception e) {
            log.error("비동기 JobLauncher 초기화에 실패했습니다.", e);
        }

        return jobLauncher;
    }
}
