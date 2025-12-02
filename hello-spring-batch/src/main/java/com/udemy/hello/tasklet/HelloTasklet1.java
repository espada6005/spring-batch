package com.udemy.hello.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component("HelloTasklet1")
@StepScope
@Slf4j
public class HelloTasklet1 implements Tasklet {

	@Value("#{jobParameters['param1']}")
	private String param1;

	@Value("#{jobParameters['param2']}")
	private Integer param2;

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {

		log.info("Hello, Tasllet1 !!!");
		log.info("param1={}", param1);
		log.info("param2={}", param2);

		ExecutionContext jobContext = contribution.getStepExecution()
				.getJobExecution()
				.getExecutionContext();
		jobContext.put("jobKey1", "jobValue1");

		return RepeatStatus.FINISHED;
	}

}
