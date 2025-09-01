package com.graydang.app.batch.bill.jobs.summarize;

import com.graydang.app.batch.bill.jobs.summarize.processor.SummarizeBillItemProcessor;
import com.graydang.app.domain.bill.model.Bill;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SummarizeBillJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job summarizeBillJob(Step summarizeBillStep) {
        return new JobBuilder("summarizeBillJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(summarizeBillStep)
                .build();
    }

    @Bean Step summarizeBillStep(JpaCursorItemReader<Bill> summarizeBillItemReader,
                                 SummarizeBillItemProcessor summarizeBillItemProcessor,
                                 JpaItemWriter<Bill> summarizeBillItemWriter) {
        return new StepBuilder("summarizeBillStep", jobRepository)
                .<Bill, Bill>chunk(10, txManager)  // << GPT API 호출은 느리므로 chunk 사이즈 작게 유지
                .reader(summarizeBillItemReader)
                .processor(summarizeBillItemProcessor)
                .writer(summarizeBillItemWriter)
                .build();
    }

    @Bean
    public JpaCursorItemReader<Bill> summarizeBillItemReader() {
        // aiProcessed 필드가 false인 의안을 읽어오는 reader
        return new JpaCursorItemReaderBuilder<Bill>()
                .name("summarizeBillItemReader")
                .entityManagerFactory(entityManagerFactory)
                //.pageSize(10) // chunk 사이즈와 동일하게 설정
                .queryString("SELECT b FROM Bill b WHERE b.aiProcessed = false ORDER BY b.id ASC")
                .build();
    }

    // Processor는 별도 클래스로 관리하므로 여기서는 주입만 받는다.

    @Bean
    public JpaItemWriter<Bill> summarizeBillItemWriter() {
        // Processor가 넘겨준 수정된 Bill 엔티티를 DB에 반영(Update)하는 Writer
        return new JpaItemWriterBuilder<Bill>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
