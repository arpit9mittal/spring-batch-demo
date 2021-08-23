package a9m.demo.batch.config.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.file.transform.PatternMatchingCompositeLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import a9m.demo.batch.config.JobCompletionNotificationListener;
import a9m.demo.batch.config.readers.TradeFieldSetMapper;
import a9m.demo.batch.config.readers.multiline.AggregateItem;
import a9m.demo.batch.config.readers.multiline.AggregateItemFieldSetMapper;
import a9m.demo.batch.config.readers.multiline.AggregateItemReader;
import a9m.demo.batch.config.readers.multiline.DequeueItemReader;
import a9m.demo.batch.config.readers.multiline.MultiLineFileProperties;
import a9m.demo.batch.entity.Trade;
import a9m.demo.batch.util.BatchUtils;

@Configuration
public class MultiLineJobConfiguration {
	
	private static String ASTERISK = "*";
	private static String BODY = "BODY";
	private static String TOKENIZER_NAME = "col";
	private static String TOKENIZER_RANGE = "range";

	private static final Logger log = LoggerFactory.getLogger(MultiLineJobConfiguration.class);
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	public MultiLineFileProperties properties;
	
	/**
	 * @return
	 */
	@SuppressWarnings("unused")
	private PatternMatchingCompositeLineTokenizer fixedFileDescriptor1() {
		PatternMatchingCompositeLineTokenizer tokenizer = new PatternMatchingCompositeLineTokenizer();

		Map<String, LineTokenizer> tokenizers = new HashMap<String, LineTokenizer>() {
			private static final long serialVersionUID = -9077589837403432940L;

			{
				put("BEGIN*", BatchUtils.getTokenizer(null, "1-5"));
				put("END*", BatchUtils.getTokenizer(null, "1-3"));
				put("*", BatchUtils.getTokenizer("ISIN,Quantity,Price,Customer", "1-12,13-15,16-20,21-29"));
			}
		};

		tokenizer.setTokenizers(tokenizers);
		return tokenizer;
	}
	
	/**
	 * @param name
	 * @return
	 */
	private String getTokenizerKey(String name) {
		String tokenizerKey = name;

		if (BODY.equalsIgnoreCase(name))
			tokenizerKey = ASTERISK;
		else
			tokenizerKey.concat(ASTERISK);

		return tokenizerKey;
	}
	
	private PatternMatchingCompositeLineTokenizer fixedFileDescriptor() {
		PatternMatchingCompositeLineTokenizer tokenizer = new PatternMatchingCompositeLineTokenizer();

		Map<String, LineTokenizer> tokenizers = new HashMap<String, LineTokenizer>();

		for (Map.Entry<String, Map<String, List<String>>> entry : properties.getTokenizer().entrySet()) {
			System.out.println(entry.getKey() + "/" + entry.getValue());

			tokenizers.put(getTokenizerKey(entry.getKey()), BatchUtils.getTokenizer(entry.getValue().get(TOKENIZER_NAME), entry.getValue().get(TOKENIZER_RANGE)));
		}

		tokenizer.setTokenizers(tokenizers);
		return tokenizer;
	}
	
	private AggregateItemFieldSetMapper<Trade> fieldSetMapper() {
		AggregateItemFieldSetMapper<Trade> fieldSetMapper = new AggregateItemFieldSetMapper<>();
		fieldSetMapper.setDelegate(new TradeFieldSetMapper());
		
		return fieldSetMapper;
	}
	
	
	@Bean
	@StepScope
	public FlatFileItemReader<AggregateItem<Trade>> tradeFileItemReader(
			@Value("#{jobParameters['in.file.name']}") String fileName) {
		
		return new FlatFileItemReaderBuilder<AggregateItem<Trade>>()
			.name("tradeFileItemReader")
			.resource(new ClassPathResource(fileName))
			.lineTokenizer(fixedFileDescriptor())
			.fieldSetMapper(fieldSetMapper())
			.build();
	}
	
	@Bean
	public ItemReader<Trade> tradeReader(FlatFileItemReader<AggregateItem<Trade>> tradeFileItemReader) {
		
		AggregateItemReader<Trade> aggregateItemReader = new AggregateItemReader<>();
		aggregateItemReader.setItemReader(tradeFileItemReader);
		
		return new DequeueItemReader<>(aggregateItemReader);
	}
	
	@Bean
	@StepScope
	public FlatFileItemWriter<Trade> tradeWriter(
			@Value("#{jobParameters['out.file.name']}") String fileName) {
		return new FlatFileItemWriterBuilder<Trade>()
				.name("tradeWriter")
				.resource(new FileSystemResource(fileName))
				.lineAggregator(new PassThroughLineAggregator<Trade>())
				.build();
	}
	
	@Bean
	public ItemProcessor<Trade, Trade> tradeProcessor() {
		return new ItemProcessor<Trade, Trade>() {

			@Override
			public Trade process(Trade item) throws Exception {
				log.info("In processor: "+item.getCustomer());
				item.setProcessed(true);
				return item;
			}
		};
	}
	
	@Bean
	public Step multilineStep(
			@Value("${batch.step.chunk.size}") int size ,
			@Qualifier("tradeReader") ItemReader<Trade> reader, 
			@Qualifier("tradeProcessor") ItemProcessor<Trade, Trade> processor, 
			@Qualifier("tradeWriter") ItemWriter<Trade> writer,
			@Qualifier("simpleStepItemReadListner") ItemReadListener<Trade> itemReadListener 
			){
		
		return stepBuilderFactory.get("multiLineStep")
			.<Trade, Trade> chunk(size)
			.reader(reader)
			.processor(processor)
			.writer(writer)
			.listener(itemReadListener)
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
