package com.graydang.app.batch.bill.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BillStepLoggingListener {

    /**
     * 스텝이 시작하기 직전에 호출됩니다.
     */
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        log.info("✅ [START] Step: [{}]가 Job Parameters와 함께 시작됩니다: {}",
                stepExecution.getStepName(),
                stepExecution.getJobParameters());
    }

    /**
     * 스텝이 완료된 직후에 호출됩니다. (성공/실패 무관)
     * 이 메서드에서 StepExecution을 통해 최종 집계 결과를 알 수 있습니다.
     */
    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();

        if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("🎉 [END] Step: [{}] - 성공적으로 완료되었습니다.", stepName);
        } else if (stepExecution.getStatus() == BatchStatus.FAILED) {
            log.error("🔥 [END] Step: [{}] - 실패했습니다.", stepName);
            // 실패 시 마지막 예외 로그
            stepExecution.getFailureExceptions().forEach(e ->
                    log.error("    ㄴ Failure Exception: {}", e.getMessage())
            );
        }

        // --- (★) 사용자가 원한 최종 집계 로그 ---
        log.info("📊 [SUMMARY] Step: [{}] | " +
                        "Read: {} | " +        // (1) Reader가 읽은 총 개수
                        "Filtered: {} | " +    // (2) Processor가 필터링(null)한 개수
                        "Written: {} | " +     // (3) Writer가 최종 처리(저장)한 개수
                        "CommitCount: {}",     // (4) 총 커밋된 Chunk 수
                stepName,
                stepExecution.getReadCount(),
                stepExecution.getFilterCount(), // (Read Count - Write Count)
                stepExecution.getWriteCount(),
                stepExecution.getCommitCount());
        // ------------------------------------

        return stepExecution.getExitStatus();
    }
}
