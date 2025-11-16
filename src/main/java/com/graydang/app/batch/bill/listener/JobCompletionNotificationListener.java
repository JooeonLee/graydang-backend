package com.graydang.app.batch.bill.listener;

import com.graydang.app.monitoring.SlackNotifier;
import com.graydang.app.monitoring.SlackPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final SlackNotifier slackBotNotifier;

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        BatchStatus status = jobExecution.getStatus();

        // (1) Job의 모든 Step에서 Read/Write/Filter Count를 합산
        long readCount = 0;
        long writeCount = 0;
        long filterCount = 0;

        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        for (StepExecution step : stepExecutions) {
            readCount += step.getReadCount();
            writeCount += step.getWriteCount();
            filterCount += step.getFilterCount();
        }

        // (2) 알림 메시지 생성
        String message;
        if (status == BatchStatus.COMPLETED) {
            message = String.format(
                    "🎉 [배치 성공] Job: %s\n" +
                            "Read: %d, Written: %d, Filtered: %d",
                    jobName, readCount, writeCount, filterCount
            );
            log.info("JobCompletionNotificationListener: 배치 성공. 슬랙 알림 전송.");
        } else if (status == BatchStatus.FAILED) {
            // 실패 시 예외 메시지 포함
            String errors = jobExecution.getFailureExceptions().stream()
                    .map(Throwable::getMessage)
                    .collect(Collectors.joining(", "));

            message = String.format(
                    "🔥 [배치 실패] Job: %s\n" +
                            "Error: %s",
                    jobName, errors
            );
            log.error("JobCompletionNotificationListener: 배치 실패. 슬랙 알림 전송.");
        } else {
            // (COMPLETED, FAILED 외의 상태)
            message = String.format("🔔 [배치 알림] Job: %s, Status: %s", jobName, status);
        }

        // (3) 슬랙 메시지 전송
        SlackPayload slackPayload = new SlackPayload(message, "#monitoring-local", null);
        slackBotNotifier.send(slackPayload);
    }

    // beforeJob은 필요 없으면 비워둡니다.
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("JobCompletionNotificationListener: {} 배치를 시작합니다.", jobExecution.getJobInstance().getJobName());
    }
}
