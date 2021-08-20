/**
 * 
 */
package a9m.demo.batch.framework;

import java.util.ArrayList;

import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilderHelper;
import org.springframework.batch.core.step.item.ChunkOrientedTasklet;
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatOperations;
import org.springframework.util.Assert;

/**
 * @author s2565087
 *
 */
public class CustomSimpleStepBuilder<I, O> extends SimpleStepBuilder<I, O> {

	
	/**
	 * @param parent
	 */
	public CustomSimpleStepBuilder(StepBuilderHelper<?> parent) {
		super(parent);
	}
	
	/**
	 * @param parent
	 */
	protected CustomSimpleStepBuilder(CustomSimpleStepBuilder<I, O> parent) {
		super(parent);
	}
	
	@Override
	protected Tasklet createTasklet() {
		Assert.state(getReader() != null, "ItemReader must be provided");
		Assert.state(getWriter() != null, "ItemWriter must be provided");

		RepeatOperations repeatOperations = createChunkOperations();
		
		CustomChunkProvider<I> chunkProvider = new CustomChunkProvider<>(getReader(), repeatOperations);
		chunkProvider.setListeners(new ArrayList<>(getItemListeners()));

		SimpleChunkProcessor<I, O> chunkProcessor = new SimpleChunkProcessor<>(getProcessor(), getWriter());
		chunkProcessor.setListeners(new ArrayList<>(getItemListeners()));
		
		ChunkOrientedTasklet<I> tasklet = new ChunkOrientedTasklet<>(chunkProvider, chunkProcessor);
		tasklet.setBuffering(!isReaderTransactionalQueue());
		
		return tasklet;
	}

}
