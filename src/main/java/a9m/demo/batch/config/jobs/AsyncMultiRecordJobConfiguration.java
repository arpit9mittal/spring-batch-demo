package a9m.demo.batch.config.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
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
import a9m.demo.batch.config.readers.multiline.MultiRecordItemReader;
import a9m.demo.batch.config.readers.multiline.StepItemReadListListener;
import a9m.demo.batch.entity.Person;
import a9m.demo.batch.framework.CustomStepBuilderFactory;

@Configuration
public class AsyncMultiRecordJobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public CustomStepBuilderFactory stepBuilderFactory;
	
	
	@Bean
	public MultiRecordItemReader<Person> multiRecordItemReader(
			@Value("${batch.file.multi.record.size}") int size,
			@Qualifier("simpleDelimitedReader") FlatFileItemReader<Person> reader) {
		
		return new MultiRecordItemReader<Person>(size,reader);
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Bean
	public Step asyncMultiRecordStep(
			StepItemReadListListener itemReadListener, 
			@Qualifier("asyncWriter") ItemWriter writer,
			@Qualifier("asyncProcessor") ItemProcessor processor, 
			@Qualifier("multiRecordItemReader") ItemReader reader ){
		
		return stepBuilderFactory.get("asyncMultiRecordStep")
			.chunk(1)
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
