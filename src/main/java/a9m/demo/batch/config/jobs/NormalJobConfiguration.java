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

import a9m.demo.batch.config.JobCompletionNotificationListener;
import a9m.demo.batch.config.processors.PersonItemProcessor;
import a9m.demo.batch.config.readers.StepItemReadListener;
import a9m.demo.batch.config.writers.PersonItemWriter;
import a9m.demo.batch.entity.Person;

@Configuration
public class NormalJobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public Step normalStep(
			@Value("${batch.step.chunk.size}") int stepChunkSize,
			@Qualifier("simpleDelimitedReader") FlatFileItemReader<Person> reader, 
			PersonItemProcessor processor,
			PersonItemWriter  itemWriter,
			StepItemReadListener<Person> itemReadListener) {

		return stepBuilderFactory.get("normalStep")
			.<Person, Person> chunk(stepChunkSize)
			.reader(reader)
			.processor(processor)
			.writer(itemWriter)
			.listener(itemReadListener)
			.build();
	}
	
	@Bean
	public Job normalJob(JobCompletionNotificationListener listener, Step normalStep) {
		return jobBuilderFactory.get("normalJob")
			.incrementer(new RunIdIncrementer())
			.listener(listener)
			.flow(normalStep)
			.end()
			.build();
	}
}
