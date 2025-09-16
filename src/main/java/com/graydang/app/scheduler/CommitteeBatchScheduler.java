package com.graydang.app.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommitteeBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job billCommitteeJob; // CommitteeProcessingJobConfig에 정의된 Job Bean

    // 매일 새벽 4시에 실행되도록 설정 (cron="초 분 시 일 월 요일")
    @Scheduled(cron = "0 30 19 * * *")
    public void runBillCommitteeJob() {
        try {
            // JobParameters: 배치 작업의 고유성을 보장. 매번 다른 파라미터로 실행해야 재실행이 가능합니다.
            // 가장 간단한 방법은 현재 시간을 파라미터로 넘기는 것입니다.
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("runDate", LocalDateTime.now().toString())
                    .toJobParameters();

            log.info(">>>>>> Starting billCommitteeJob with parameters: {}", jobParameters);
            jobLauncher.run(billCommitteeJob, jobParameters);
            log.info("<<<<<< Finished billCommitteeJob successfully");

        } catch (Exception e) {
            log.error("!!!!!! Failed to run billCommitteeJob", e);
        }
    }
}
