package a9m.demo.batch.config.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import a9m.demo.batch.config.JobCompletionNotificationListener;
import a9m.demo.batch.config.processors.PersonItemProcessor;
import a9m.demo.batch.config.readers.StepItemReadListener;
import a9m.demo.batch.config.writers.PersonItemWriter;
import a9m.demo.batch.entity.Person;

@Configuration
public class MultiThreadedJobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public Step multiThreadedStep(
			@Value("${batch.step.chunk.size}") int stepChunkSize,
			@Qualifier("simpleDelimitedReader") FlatFileItemReader<Person> reader, 
			StepItemReadListener<Person> itemReadListener, 
			PersonItemProcessor processor,
			PersonItemWriter  itemWriter,
			TaskExecutor taskExecutor) {
		
		return stepBuilderFactory.get("multiThreadedStep")
			.<Person, Person> chunk(stepChunkSize)
			.reader(reader)
			.processor(processor)
			.writer(itemWriter)
			.listener(itemReadListener)
			.taskExecutor(taskExecutor)
			.build();
	}
	
	@Bean
	public Job multiThreadedJob(JobCompletionNotificationListener listener, Step multiThreadedStep) {
		return jobBuilderFactory.get("multiThreadedJob")
			.incrementer(new RunIdIncrementer())
			.listener(listener)
			.flow(multiThreadedStep)
			.end()
			.build();
	}

}
