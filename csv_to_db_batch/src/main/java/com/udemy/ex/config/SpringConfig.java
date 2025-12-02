package com.udemy.ex.config;

import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import com.udemy.ex.model.Emplpyee;

@Configuration
public class SpringConfig {

	private final JobLauncher jobLauncher;

	private final JobRepository jobRepository;

	private final PlatformTransactionManager transactionManager;

	@Value("${csv.path}")
	private Resource inputCSV;

	@Autowired
	private DataSource dataSource;

	private static final String INSERT_EMP_SQL = "INSERT INTO employee (empnumber, empname, jobtitle, "
			+ "mgrnumber, hiredate)"
			+ "VALUES (:empNumber, :empName, :jobTitle, "
			+ ":mgrNumber, :hireDate)";

	public SpringConfig(JobLauncher jobLauncher, JobRepository jobRepository,
			PlatformTransactionManager transactionManager) {
		this.jobLauncher = jobLauncher;
		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
	}

	@Bean
	@StepScope
	FlatFileItemReader<Emplpyee> csvItemReader() {

		FlatFileItemReader<Emplpyee> reader = new FlatFileItemReader<Emplpyee>();
		reader.setResource(inputCSV);
		reader.setLinesToSkip(1);
		reader.setEncoding(StandardCharsets.UTF_8.name());

		BeanWrapperFieldSetMapper<Emplpyee> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<Emplpyee>();
		beanWrapperFieldSetMapper.setTargetType(Emplpyee.class);

		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		String[] csvTitleArray = new String[] { "EmpNumber", "EmpName", "JobTitle", "MgrNumber", "HireDate" };
		tokenizer.setNames(csvTitleArray);

		DefaultLineMapper<Emplpyee> lineMapper = new DefaultLineMapper<Emplpyee>();
		lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
		lineMapper.setLineTokenizer(tokenizer);

		reader.setLineMapper(lineMapper);

		return reader;
	}

	@Autowired
	@Qualifier("EmpItemProcessor")
	public ItemProcessor<Emplpyee, Emplpyee> empItemProcessor;

	@Bean
	@StepScope
	JdbcBatchItemWriter<Emplpyee> jdbcItemWriter() {

		BeanPropertyItemSqlParameterSourceProvider<Emplpyee> provider = new BeanPropertyItemSqlParameterSourceProvider<Emplpyee>();

		JdbcBatchItemWriter<Emplpyee> writer = new JdbcBatchItemWriter<Emplpyee>();

		writer.setDataSource(dataSource);
		writer.setItemSqlParameterSourceProvider(provider);
		writer.setSql(INSERT_EMP_SQL);

		return writer;
	}

	@Bean
	Step chunStep1() {
		return new StepBuilder("EmpItemStep1", jobRepository)
				.<Emplpyee, Emplpyee> chunk(1, transactionManager)
				.reader(csvItemReader())
				.processor(empItemProcessor)
				.writer(jdbcItemWriter())
				.build();
	}

	@Bean
	Job chunkJob() {
		return new JobBuilder("chunkJob", jobRepository)
				.incrementer(new RunIdIncrementer())
				.start(chunStep1())
				.build();
	}
}
