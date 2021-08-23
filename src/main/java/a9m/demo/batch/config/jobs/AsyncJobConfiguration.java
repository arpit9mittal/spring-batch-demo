package a9m.demo.batch.config.jobs;

import java.util.concurrent.Future;

import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import a9m.demo.batch.config.JobCompletionNotificationListener;
import a9m.demo.batch.config.processors.PersonItemProcessor;
import a9m.demo.batch.config.writers.PersonItemWriter;
import a9m.demo.batch.entity.Person;
import a9m.demo.batch.entity.Trade;

@Configuration
public class AsyncJobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public AsyncItemProcessor<Person, Person> asyncProcessor(
			TaskExecutor taskExecutor,
			PersonItemProcessor processor) {
		
	    AsyncItemProcessor<Person, Person> asyncItemProcessor = new AsyncItemProcessor<Person, Person>();
	    asyncItemProcessor.setTaskExecutor(taskExecutor);
	    asyncItemProcessor.setDelegate(processor);
	    return asyncItemProcessor;
	}
	
	
	@Bean
	public AsyncItemWriter<Person> asyncWriter(PersonItemWriter itemWriter ) {
	    AsyncItemWriter<Person> asyncItemWriter = new AsyncItemWriter<Person>();
	    asyncItemWriter.setDelegate(itemWriter);
	    return asyncItemWriter;
	}
	
	@Bean
	public Step asyncStep(
			@Value("${batch.step.chunk.size}") int stepChunkSize,
			@Qualifier("simpleDelimitedReader") FlatFileItemReader<Person> reader, 
			@Qualifier("simpleStepItemReadListner") ItemReadListener<Trade> itemReadListener,
			AsyncItemProcessor<Person, Person> asyncProcessor, 
			AsyncItemWriter<Person> asyncWriter) {
		
		return stepBuilderFactory.get("asyncStep")
			.<Person, Future<Person>> chunk(stepChunkSize)
			.reader(reader)
			.processor(asyncProcessor)
			.writer(asyncWriter)
			.listener(itemReadListener)
			.build();
	}
	
	@Bean
	public Job asyncJob(JobCompletionNotificationListener listener, Step asyncStep) {
		return jobBuilderFactory.get("asyncJob")
			.incrementer(new RunIdIncrementer())
			.listener(listener)
			.flow(asyncStep)
			.end()
			.build();
	}
	
}
