package com.graydang.app.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TodayPassedBillScheduler {

    private final JobLauncher jobLauncher;
    private final Job todayPassedBillJob;

    /**
     * 매일 오후 9시에 오늘 통과된 법안을 수집하는 배치 실행
     * 
     * 이 스케줄러는 매일 오후 9시에 실행되어 당일 국회에서 통과된 법안들을 수집합니다.
     * 오후 9시로 설정한 이유는 국회 본회의가 대부분 저녁 이전에 종료되기 때문입니다.
     */
    @Scheduled(cron = "0 0 21 * * *")  // 매일 21시(오후 9시) 0분 0초
    public void runTodayPassedBillCollection() {
        log.info("📅 [오늘 통과 법안 수집 배치] 스케줄러 시작 - {}", LocalDateTime.now());
        
        try {
            // JobParameters 생성 - 매 실행마다 고유한 파라미터 필요
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobName", "todayPassedBillJob")
                    .addLong("executionTime", System.currentTimeMillis())
                    .addString("executionDate", LocalDateTime.now().toString())
                    .toJobParameters();

            // Job 실행
            JobExecution jobExecution = jobLauncher.run(todayPassedBillJob, jobParameters);
            
            log.info("✅ [오늘 통과 법안 수집 배치] 실행 완료 - Status: {}, Exit Status: {}", 
                    jobExecution.getStatus(), 
                    jobExecution.getExitStatus());

            // 실행 결과에 따른 추가 로그
            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                log.info("🎉 [오늘 통과 법안 수집 배치] 성공적으로 완료되었습니다.");
            } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
                log.error("❌ [오늘 통과 법안 수집 배치] 실패했습니다. 상세 내용을 확인하세요.");
            } else {
                log.warn("⚠️ [오늘 통과 법안 수집 배치] 비정상 종료 - Status: {}", jobExecution.getStatus());
            }
            
        } catch (Exception e) {
            log.error("🚨 [오늘 통과 법안 수집 배치] 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 수동 실행을 위한 메소드 (테스트 목적)
     * 이 메소드는 API 엔드포인트나 다른 서비스에서 호출하여 수동으로 배치를 실행할 수 있습니다.
     */
    public void runManually() {
        log.info("🔧 [오늘 통과 법안 수집 배치] 수동 실행 요청");
        runTodayPassedBillCollection();
    }
}