package com.graydang.app.batch.bill.controller;

import com.graydang.app.global.common.model.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/batch")
public class BillStatusHistoryBatchController {

    private final JobLauncher asyncJobLauncher;
    private final Job billStatusHistoryCommitteeUpdateJob;
    private final Job billStatusHistoryCommitteeCompletionJob;
    private final Job billStatusHistoryCommitteeUpdateAsyncJob;
    private final Job billStatusHistoryCommitteeCompletionAsyncJob;
    private final Job billStatusHistoryPlenaryInProgressAsyncJob;

    @PostMapping("/run-committee-in-progress-job")
    public ResponseEntity<BaseResponse<String>> runCommitteeInProgressJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("triggerTime", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();

            log.info("API 요청으로 'billStatusHistoryCommitteeUpdateJob' 배치를 시작합니다...");

            // (4) 비동기 JobLauncher로 실행
            asyncJobLauncher.run(billStatusHistoryCommitteeUpdateJob, jobParameters);

            // (5) Job이 시작되었다고 즉시 응답
            return ResponseEntity.ok(new BaseResponse<>("Batch job 'billStatusHistoryCommitteeUpdateJob' 이 비동기로 시작되었습니다."));

        } catch (Exception e) {
            log.error("배치 Job 실행 중 오류 발생", e);
            return ResponseEntity.status(500)
                    .body(new BaseResponse<>("Job 실행 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/run-committee-completion-job")
    public ResponseEntity<BaseResponse<String>> runCommitteeCompletionJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("triggerTime", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();

            log.info("API 요청으로 'billStatusHistoryCommitteeCompletionJob' 배치를 시작합니다...");

            // (4) 비동기 JobLauncher로 실행
            asyncJobLauncher.run(billStatusHistoryCommitteeCompletionJob, jobParameters);

            // (5) Job이 시작되었다고 즉시 응답
            return ResponseEntity.ok(new BaseResponse<>("Batch job 'billStatusHistoryCommitteeCompletionJob' 이 비동기로 시작되었습니다."));

        } catch (Exception e) {
            log.error("배치 Job 실행 중 오류 발생", e);
            return ResponseEntity.status(500)
                    .body(new BaseResponse<>("Job 실행 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/run-committee-in-progress-async-job")
    public ResponseEntity<BaseResponse<String>> runCommitteeInProgressAsyncJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("triggerTime", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();

            log.info("API 요청으로 'billStatusHistoryCommitteeUpdateAsyncJob' 배치를 시작합니다...");

            // (4) 비동기 JobLauncher로 실행
            asyncJobLauncher.run(billStatusHistoryCommitteeUpdateAsyncJob, jobParameters);

            // (5) Job이 시작되었다고 즉시 응답
            return ResponseEntity.ok(new BaseResponse<>("Batch job 'billStatusHistoryCommitteeUpdateAsyncJob' 이 비동기로 시작되었습니다."));

        } catch (Exception e) {
            log.error("배치 Job 실행 중 오류 발생", e);
            return ResponseEntity.status(500)
                    .body(new BaseResponse<>("Job 실행 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/run-committee-completion-async-job")
    public ResponseEntity<BaseResponse<String>> runCommitteeCompletionAsyncJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("triggerTime", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();

            log.info("API 요청으로 'billStatusHistoryCommitteeCompletionAsyncJob' 배치를 시작합니다...");

            // (4) 비동기 JobLauncher로 실행
            asyncJobLauncher.run(billStatusHistoryCommitteeCompletionAsyncJob, jobParameters);

            // (5) Job이 시작되었다고 즉시 응답
            return ResponseEntity.ok(new BaseResponse<>("Batch job 'billStatusHistoryCommitteeCompletionAsyncJob' 이 비동기로 시작되었습니다."));

        } catch (Exception e) {
            log.error("배치 Job 실행 중 오류 발생", e);
            return ResponseEntity.status(500)
                    .body(new BaseResponse<>("Job 실행 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/run-plenary-session-in-progress-async-job")
    public ResponseEntity<BaseResponse<String>> runPlenaryInProgressAsyncJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("triggerTime", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();

            log.info("API 요청으로 'billStatusHistoryPlenaryInProgressAsyncJob' 배치를 시작합니다...");

            // 비동기 JobLauncher로 실행
            asyncJobLauncher.run(billStatusHistoryPlenaryInProgressAsyncJob, jobParameters);

            // Job이 시작되었다고 즉시 응답
            return ResponseEntity.ok(new BaseResponse<>("Batch job 'billStatusHistoryPlenaryInProgressAsyncJob' 이 비동기로 시작되었습니다."));

        } catch (Exception e) {
            log.error("배치 Job 실행 중 오류 발생", e);
            return ResponseEntity.status(500)
                    .body(new BaseResponse<>("Job 실행 실패: " + e.getMessage()));
        }
    }
}
