package a9m.demo.batch.config.readers.multiline;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.lang.Nullable;

/**
 * An {@link ItemReader} that delivers a list as its item, storing up objects
 * from the injected {@link ItemReader} until they are ready to be packed out as
 * a collection. This class must be used as a wrapper for a custom
 * {@link ItemReader}. 
 * 
 * This class is thread-safe (it can be used concurrently by multiple threads)
 * as long as the {@link ItemReader} is also thread-safe.
 * 
 */
public class MultiRecordItemReader<T> implements ItemStreamReader<List<T>> {
	
	private static final Logger log = LoggerFactory.getLogger(MultiRecordItemReader.class);

	private int numberOfRecords;
	private ItemStreamReader<T> itemReader;
	
	
	/**
	 * @param numberOfRecords
	 */
	public MultiRecordItemReader(int numberOfRecords) {
		super();
		this.numberOfRecords = numberOfRecords;
	}

	/**
	 * @param numberOfRecords
	 * @param itemReader
	 */
	public MultiRecordItemReader(int numberOfRecords, ItemStreamReader<T> itemReader) {
		super();
		this.numberOfRecords = numberOfRecords;
		this.itemReader = itemReader;
	}


	/**
	 * Get the list of records.
	 *
	 * @see org.springframework.batch.item.ItemReader#read()
	 */
	@Nullable
	@Override
	public List<T> read() throws Exception {
		List<T> records = new ArrayList<>();
		
		System.out.println("Reading "+numberOfRecords+" records" );
		log.info("Reading {} records", numberOfRecords);
		
		
	    while(records.size() < numberOfRecords) {
	        T record = itemReader.read();
	        if (Objects.isNull(record)) {
	            break;
	        }
	        records.add(record);
	    }

	    if (records.isEmpty()) {
	        return null;
	    }

	    return records;
	}
	

	/**
	 * @return
	 */
	public int getNumberOfRecords() {
		return numberOfRecords;
	}

	/**
	 * @param itemReader
	 */
	public void setItemReader(ItemStreamReader<T> itemReader) {
		this.itemReader = itemReader;
	}
	
	/**
	 * @param numberOfRecords
	 */
	public void setNumberOfRecords(int numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		itemReader.open(executionContext);
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		itemReader.update(executionContext);		
	}

	@Override
	public void close() throws ItemStreamException {
		itemReader.close();
	}

}