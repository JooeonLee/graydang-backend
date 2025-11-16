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

    @PostMapping("/run-committee-registration-job")
    public ResponseEntity<BaseResponse<String>> runCommitteeRegistrationJob() {
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
}
