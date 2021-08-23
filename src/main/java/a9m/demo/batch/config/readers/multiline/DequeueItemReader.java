package a9m.demo.batch.config.readers.multiline;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 * @author s2565087
 *
 * @param <T>
 */
public class DequeueItemReader<T> implements ItemStreamReader<T> {
	
	private Deque<T> queue ;
	private ItemStreamReader<List<T>> itemReader;
	
	private static final Logger log = LoggerFactory.getLogger(DequeueItemReader.class);

	
	public DequeueItemReader() {
		super();
		this.queue = new LinkedList<>();
	}

	/**
	 * @param itemReader
	 */
	public DequeueItemReader(ItemStreamReader<List<T>> itemReader) {
		super();
		this.itemReader = itemReader;
		this.queue = new LinkedList<>();
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

	@Override
	public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		log.info("Is Queue empty: " + queue.isEmpty());
		if (queue.isEmpty()) {
			List<T> items = itemReader.read();
			if (items != null) {
				log.info("Aggregated read size: " +items.size());
				queue.addAll(items);
			}
		}

		log.info("Queue size: " + queue.size());
		return queue.poll();
	}

	/**
	 * @return
	 */
	public ItemStreamReader<List<T>> getItemReader() {
		return itemReader;
	}

	/**
	 * @param itemReader
	 */
	public void setItemReader(ItemStreamReader<List<T>> itemReader) {
		this.itemReader = itemReader;
	}

}
