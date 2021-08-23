package a9m.demo.batch.config.jobs;

import java.util.concurrent.Future;

import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import a9m.demo.batch.config.JobCompletionNotificationListener;
import a9m.demo.batch.config.readers.multiline.DequeueItemReader;
import a9m.demo.batch.config.readers.multiline.MultiRecordItemReader;
import a9m.demo.batch.entity.Person;
import a9m.demo.batch.entity.Trade;

@Configuration
public class AsyncMultiRecordJobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public ItemReader<Person> multiRecordItemReader(
			@Value("${batch.file.multi.record.size}") int size,
			@Qualifier("simpleDelimitedReader") FlatFileItemReader<Person> reader) {
		
		MultiRecordItemReader<Person> itemReader = new MultiRecordItemReader<Person>(size,reader);
		return new DequeueItemReader<>(itemReader);
	}
	
	@Bean
	public Step asyncMultiRecordStep(
			@Value("${batch.file.multi.record.size}") int size,
			@Qualifier("multiRecordItemReader") ItemReader<Person> reader,
			@Qualifier("asyncProcessor") ItemProcessor<Person, Future<Person>> processor,
			@Qualifier("asyncWriter") ItemWriter<Future<Person>> writer,
			@Qualifier("simpleStepItemReadListner") ItemReadListener<Trade> itemReadListener) {

		return stepBuilderFactory.get("asyncMultiRecordStep")
			.<Person,Future<Person>> chunk(size)
			.reader(reader)
			.processor(processor)
			.writer(writer)
			.listener(itemReadListener)
			.build();
	}
	
	@Bean
	public Job asyncMultiRecordJob(JobCompletionNotificationListener listener, Step asyncMultiRecordStep) {
		return jobBuilderFactory.get("asyncMultiRecordJob")
			.incrementer(new RunIdIncrementer())
			.listener(listener)
			.flow(asyncMultiRecordStep)
			.end()
			.build();
	}
	
}
