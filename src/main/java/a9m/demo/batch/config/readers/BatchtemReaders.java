package a9m.demo.batch.config.readers;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import a9m.demo.batch.entity.Person;

@Configuration
public class BatchtemReaders {

	@Bean
	@StepScope
	public FlatFileItemReader<Person> simpleDelimitedReader(
			@Value("#{jobParameters['in.file.name']}") String fileName,
			@Value("#{jobParameters['in.file.col']}") String fileCol
			) {
		return new FlatFileItemReaderBuilder<Person>()
			.name("personItemReader")
			.resource(new ClassPathResource(fileName))
			.delimited()
			.names(fileCol.split(","))
			.fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
				setTargetType(Person.class);
			}})
			.build();
	}
}
