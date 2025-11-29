package com.graydang.app.batch.bill.jabs.update.committee;

import com.graydang.app.batch.bill.client.BillApiClient;
import com.graydang.app.batch.bill.dto.BillCommissionResponseDto;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles({"test", "oauth"})
public class CommitteeUpdateJobConnectionTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("billStatusHistoryCommitteeUpdateJob")
    private Job job;

    @Autowired
    @Qualifier("jobLauncher")
    private JobLauncher jobLauncher;

    @MockBean
    private BillApiClient billApiClient;

    // Processor 내부의 sleep(50ms) + API 지연(100ms) = 총 150ms 지연 상황 연출
    private static final long MOCK_API_LATENCY = 100L;

    @BeforeEach
    public void setup() {
        // 1. 테스트 대상 Job 설정
        jobLauncherTestUtils.setJob(job);

        // 2. 어떤 JobLauncher를 쓸지 명시적으로 지정
        jobLauncherTestUtils.setJobLauncher(jobLauncher);

        // 3. 테이블 초기화 (순서 중요: 자식 -> 부모)
        jdbcTemplate.update("DELETE FROM bill_status_history");
        jdbcTemplate.update("DELETE FROM bill");

        // 4. INSERT 쿼리 준비
        // - NOT NULL 컬럼: id, created_at, updated_at, bill_id, status, title, ai_processed, view_count
        // - NULL 컬럼(배치가 채워줄 값): committee_name (일부러 NULL로 둠)
        String sql = """
            INSERT INTO bill (
                id, 
                bill_id, 
                title, 
                status, 
                bill_status,
                representative_name,
                propose_date,
                created_at, 
                updated_at, 
                ai_processed, 
                view_count,
                committee_name
            ) VALUES (
                ?,              -- id
                ?,              -- bill_id
                ?,              -- title
                ?,              -- status (시스템 관리용)
                ?,              -- bill_status (진행중 등)
                ?,              -- representative_name
                CURRENT_DATE,   -- propose_date
                NOW(),          -- created_at
                NOW(),          -- updated_at
                0,              -- ai_processed (Default 0)
                0,              -- view_count (Default 0)
                NULL            -- committee_name (배치가 업데이트할 대상이므로 NULL)
            )
        """;

        // 5. 데이터 100개 삽입
        for (int i = 1; i <= 1000; i++) {
            jdbcTemplate.update(sql,
                    (long) i,              // id
                    "BILL_ID_" + i,        // bill_id
                    "테스트 의안 제목 " + i,   // title
                    "WAITING",             // status (시스템 상태)
                    "접수",                 // bill_status
                    "홍길동" + i             // representative_name
            );
        }
    }

    @Test
    @DisplayName("기존 방식 측정: 멀티스레드 Step은 API 응답 대기 중에도 DB 커넥션을 점유한다")
    void measureDbConnectionLeak() throws Exception {

        // 1. [Mocking] 외부 API 호출 시 지연 시간 발생 시뮬레이션
        given(billApiClient.getBillCommissionInfo(any())).willAnswer(invocation -> {
            Thread.sleep(MOCK_API_LATENCY); // API가 느리다고 가정
            return Optional.of(createMockResponse());
        });

        JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParametersBuilder()
                .addString("requestDate", "2024-11-22") // [직접 추가한 파라미터]
                .toJobParameters();

        // 2. [Monitoring] 백그라운드 스레드에서 DB 커넥션 개수 감시
        AtomicInteger maxActiveConnections = new AtomicInteger(0);
        AtomicBoolean isRunning = new AtomicBoolean(true);

        Thread monitorThread = new Thread(() -> {
            try {
                // HikariCP의 모니터링 Bean 가져오기
                HikariPoolMXBean poolMXBean = ((HikariDataSource) dataSource).getHikariPoolMXBean();

                while (isRunning.get()) {
                    int activeConnections = poolMXBean.getActiveConnections();
                    // 최대값 갱신
                    maxActiveConnections.updateAndGet(max -> Math.max(max, activeConnections));

                    Thread.sleep(10); // 0.01초마다 체크
                }
            } catch (InterruptedException e) {
                // 종료 시 무시
            } catch (ClassCastException e) {
                System.err.println("DataSource가 HikariCP가 아닙니다. 커넥션 측정이 불가능합니다.");
            }
        });
        monitorThread.start();

        // 3. [Execution] 배치 실행
        System.out.println("============== [배치 실행 시작] ==============");
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        System.out.println("============== [배치 실행 종료] ==============");

        // 4. [Stop] 모니터링 종료
        isRunning.set(false);
        monitorThread.join();

        // 5. [Result] 결과 출력 및 검증
        int maxConn = maxActiveConnections.get();
        System.out.println("##################################################");
        System.out.println("📊 측정 결과 (Multi-threaded Step)");
        System.out.println("⏱️ Mock API 지연 시간: " + MOCK_API_LATENCY + "ms");
        System.out.println("🔌 최대 DB 커넥션 사용량: " + maxConn + "개");
        System.out.println("##################################################");

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // [검증 포인트]
        // 기존 코드는 CorePoolSize(10)에 가깝게 커넥션을 점유해야 정상입니다.
        // 따라서 커넥션이 5개 이상 사용되었는지 확인합니다.
        assertThat(maxConn)
                .as("기존 방식은 API 대기 시간 동안 DB 커넥션을 점유하므로 수치가 높아야 합니다.")
                .isGreaterThanOrEqualTo(5);
    }

    // 테스트용 더미 응답 생성 헬퍼 메서드
    private BillCommissionResponseDto createMockResponse() {
        // 1. 가장 안쪽의 Item 객체 생성
        BillCommissionResponseDto.JurisdictionExaminationItem item = new BillCommissionResponseDto.JurisdictionExaminationItem();
        item.setCommitteeName("테스트 위원회"); // DB에 업데이트될 값
        item.setSubmitDt("2024-01-01");      // [중요] Writer에서 이 값이 없으면 skip 하도록 되어 있으므로 필수!
        item.setProcResultCd("접수");         // (선택) 기타 필요한 필드

        // 2. Body 객체 생성 및 Item 리스트 설정
        BillCommissionResponseDto.BodyDto body = new BillCommissionResponseDto.BodyDto();
        body.setJurisdictionExamination(List.of(item));

        // 3. Header 객체 생성 (필수는 아니지만 Null Pointer 방지용)
        BillCommissionResponseDto.HeaderDto header = new BillCommissionResponseDto.HeaderDto();
        header.setResultCode("00");
        header.setResultMsg("NORMAL SERVICE");

        // 4. 전체 Response DTO 조립
        BillCommissionResponseDto response = new BillCommissionResponseDto();
        response.setHeader(header);
        response.setBody(body);

        return response;
    }
}
