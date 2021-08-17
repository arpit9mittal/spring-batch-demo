package my.demo.batch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.file.transform.PatternMatchingCompositeLineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import my.demo.batch.multiline.AggregateItem;
import my.demo.batch.multiline.AggregateItemFieldSetMapper;
import my.demo.batch.multiline.AggregateItemReader;
import my.demo.batch.multiline.Trade;
import my.demo.batch.multiline.TradeFieldSetMapper;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);
	
	@Autowired
	public JobLauncher jobLauncher ;

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public JobExecution run(
			JobLauncher jobLauncher, 
			@Qualifier("multilineJob") Job job) {
		
	    JobExecution jobExecution = null;
	    
	    try {
	        JobParameters jobParameters = new JobParametersBuilder()
	                .addLong("time", System.currentTimeMillis())
	                .toJobParameters();

	        jobExecution = jobLauncher.run(job, jobParameters);
	        log.info("Exit Status : " + jobExecution.getStatus());
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return jobExecution;
	}

	/**
	 * @param names
	 * @param range
	 * @return
	 */
	public FixedLengthTokenizer getTokenizer(String names, String range) {
		FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
		
		if(Strings.isNotBlank(range)) {
			List<Range> ranges =
			 Stream.of(range.split(",")).map(str -> {
				String[] arr = str.split("-");
				return new Range(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
			}).collect(Collectors.toList());
			tokenizer.setColumns(ranges.toArray(new Range[0]));
		}
		
		if(Strings.isNotBlank(names))
			tokenizer.setNames(names.split(","));
		
		return tokenizer;
	}
	
	@Bean
	PatternMatchingCompositeLineTokenizer fixedFileDescriptor() {
		PatternMatchingCompositeLineTokenizer tokenizer = new PatternMatchingCompositeLineTokenizer() ;
		
		Map<String, LineTokenizer> tokenizers= new HashMap<String, LineTokenizer>(){
			private static final long serialVersionUID = -9077589837403432940L;

			{
	            put("BEGIN*", getTokenizer(null, "1-5"));
	            put("END*", getTokenizer(null, "1-3"));
	            put("*",getTokenizer("ISIN,Quantity,Price,Customer", "1-12,13-15,16-20,21-29"));
	        }};
	        
		tokenizer.setTokenizers(tokenizers);
		return tokenizer; 
	}
	
	@Bean
	AggregateItemFieldSetMapper<Trade> fieldSetMapper() {
		AggregateItemFieldSetMapper<Trade> fieldSetMapper = new AggregateItemFieldSetMapper<>();
		fieldSetMapper.setDelegate(new TradeFieldSetMapper());
		
		return fieldSetMapper;
	}
	
	@Bean
	public FlatFileItemReader<AggregateItem<Trade>> fileItemReader(
			PatternMatchingCompositeLineTokenizer fixedFileDescriptor,
			AggregateItemFieldSetMapper<Trade> fieldSetMapper) {
		
		return new FlatFileItemReaderBuilder<AggregateItem<Trade>>()
			.name("ItemReader")
			.resource(new ClassPathResource("sample-data.csv"))
			.lineTokenizer(fixedFileDescriptor)
			.fieldSetMapper(fieldSetMapper)
			.build();
	}
	
	@Bean
	public AggregateItemReader<Trade> Reader(FlatFileItemReader<AggregateItem<Trade>> fileItemReader) {
		AggregateItemReader<Trade> aggregateItemReader = new AggregateItemReader<>();
		aggregateItemReader.setItemReader(fileItemReader);
		
		return aggregateItemReader;
	}
	
	@Bean
	public FlatFileItemWriter<Trade> writer() {
		return new FlatFileItemWriterBuilder<Trade>()
				.name("itemWriter")
				.resource(new FileSystemResource("out/sample-response.csv"))
				.lineAggregator(new PassThroughLineAggregator<Trade>())
				.build();
	}
	
	@Bean
	public ItemProcessor<Trade, Trade> processor() {
		return new ItemProcessor<Trade, Trade>() {

			@Override
			public Trade process(Trade item) throws Exception {
				item.setProcessed(true);
				return item;
			}
		};
	}
	
	
//	@Bean
//	public Step multilineStep(
//			AggregateItemReader<Trade> reader, 
//			StepItemReadListener itemReadListener, 
//			FlatFileItemWriter<Trade> writer) {
//		return stepBuilderFactory.get("multiLineStep")
//			.<Trade, Trade> chunk(1)
//			.reader(reader)
//			.writer(writer)
//			.listener(itemReadListener)
//			.build();
//	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public Step multilineStep(
			AggregateItemReader reader, 
			ItemProcessor processor,
			FlatFileItemWriter writer,
			StepItemReadListener itemReadListener) {
		return stepBuilderFactory.get("multiLineStep")
				.chunk(2)
				.reader(reader)
				.writer(writer)
				.processor(processor)
				.build();
	}
	
	@Bean
	public Job multilineJob(JobCompletionNotificationListener listener, Step multilineStep) {
		return jobBuilderFactory.get("multilineJob")
			.incrementer(new RunIdIncrementer())
			.listener(listener)
			.flow(multilineStep)
			.end()
			.build();
	}
	
}
