/**
 * 
 */
package a9m.demo.batch.framework;

import java.util.Collection;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.metrics.BatchMetrics;
import org.springframework.batch.core.step.item.Chunk;
import org.springframework.batch.core.step.item.SimpleChunkProvider;
import org.springframework.batch.core.step.item.SkipOverflowException;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.repeat.RepeatCallback;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatOperations;
import org.springframework.batch.repeat.RepeatStatus;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

/**
 * 
 * @author s2565087
 *
 */
public class CustomChunkProvider<I>  extends SimpleChunkProvider<I> {

	private final RepeatOperations repeatOperations;

	protected final ItemReader<? extends I> itemReader;

	/**
	 * @param itemReader
	 * @param repeatOperations
	 */
	public CustomChunkProvider(ItemReader<? extends I> itemReader, RepeatOperations repeatOperations) {
		super(itemReader, repeatOperations);
		this.itemReader = itemReader;
		this.repeatOperations = repeatOperations;
	}

	@Override
	public Chunk<I> provide(final StepContribution contribution) throws Exception {

		final Chunk<I> inputs = new Chunk<>();
		repeatOperations.iterate(new RepeatCallback() {

			@SuppressWarnings("unchecked")
			@Override
			public RepeatStatus doInIteration(final RepeatContext context) throws Exception {
				I item = null;
				Timer.Sample sample = Timer.start(Metrics.globalRegistry);
				String status = BatchMetrics.STATUS_SUCCESS;
				try {
					item = read(contribution, inputs);
				}
				catch (SkipOverflowException e) {
					// read() tells us about an excess of skips by throwing an
					// exception
					status = BatchMetrics.STATUS_FAILURE;
					return RepeatStatus.FINISHED;
				}
				finally {
					stopTimer(sample, contribution.getStepExecution(), status);
				}
				if (item == null) {
					inputs.setEnd();
					return RepeatStatus.FINISHED;
				}
				
				// Handle multiLine reads
				
				if (item instanceof Collection<?>) {
					((Collection<?>) item).stream().forEach(i -> inputs.add((I) i));
				} else {
					inputs.add(item);
				}
				
				contribution.incrementReadCount();
				return RepeatStatus.CONTINUABLE;
			}

		});

		return inputs;
	}
	
	private void stopTimer(Timer.Sample sample, StepExecution stepExecution, String status) {
		sample.stop(BatchMetrics.createTimer("item.read", "Item reading duration",
				Tag.of("job.name", stepExecution.getJobExecution().getJobInstance().getJobName()),
				Tag.of("step.name", stepExecution.getStepName()),
				Tag.of("status", status)
		));
	}

}