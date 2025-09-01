package com.graydang.app.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job collectRecentBillsJob;
    private final Job summarizeBillJob;
    private final AtomicReference<LocalDate> lastSuccessfulRunDate = new AtomicReference<>();

    @Scheduled(cron = "0 0 19 * * *")
    public void runBillJobs() {
        LocalDate today = LocalDate.now();
        LocalDate lastRunDate = lastSuccessfulRunDate.get();

        // --- 1. 실행 조건 확인 ---
        // 마지막 실행일이 없거나, 마지막 실행일로부터 일정 이상 지났는지 확인
        if(lastRunDate == null || ChronoUnit.DAYS.between(lastRunDate, today) >= 0) {
            log.info("매일 19시가 되어 의안 수집 및 요약 배치를 시작합니다.");
            try {
                // --- 2. 배치 job 실행 ---
                JobParameters collectParams = new JobParametersBuilder()
                        .addString("jobName", "collectRecentBillsJob")
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters();

                JobExecution collectExecution = jobLauncher.run(collectRecentBillsJob, collectParams);
                log.info("의안 수집 배치(collectRecentBillsJob) 실행 완료. Status: {}", collectExecution.getStatus());

                if (collectExecution.getStatus() == BatchStatus.COMPLETED) {
                    JobParameters summarizeParams = new JobParametersBuilder()
                            .addString("jobName", "summarizeBillJob")
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters();

                    JobExecution summarizeExecution = jobLauncher.run(summarizeBillJob, summarizeParams);
                    log.info("AI 요약 배치(summarizeBillJob) 실행 완료. Status: {}", summarizeExecution.getStatus());

                    // --- 3. 두 Job이 모두 성공했을 때만 마지막 실행 날짜를 오늘로 업데이트 ---
                    if (summarizeExecution.getStatus() == BatchStatus.COMPLETED) {
                        lastSuccessfulRunDate.set(today);
                        log.info("모든 배치가 성공하여 마지막 실행일을 {}로 업데이트했습니다.", today);
                    } else {
                        log.error("AI 요약 배치가 실패하여 마지막 실행일을 업데이트하지 않습니다.");
                    }
                } else {
                    log.error("의안 수집 배치가 실패하여 마지막 실행일을 업데이트하지 않습니다.");
                }
            } catch (Exception e) {
                log.error("배치 스케줄러 실행 중 오류 발생", e);
            }
        } else {
            // 실행일이 아니면 로그만 남기고 종료
            log.info("배치 실행일이 아닙니다. 다음 실행까지 {}일 남았습니다.", 14 - ChronoUnit.DAYS.between(lastRunDate, today));
        }
    }
}
