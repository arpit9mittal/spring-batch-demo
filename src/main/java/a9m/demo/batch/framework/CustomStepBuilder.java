/**
 * 
 */
package a9m.demo.batch.framework;

import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.CompletionPolicy;

/**
 * @author s2565087
 *
 */
public class CustomStepBuilder extends StepBuilder {
	
	/**
	 * @param name
	 */
	public CustomStepBuilder(String name) {
		super(name);
	}
	
	@Override
	public <I, O> SimpleStepBuilder<I, O> chunk(int chunkSize) {
		return new CustomSimpleStepBuilder<I, O>(this).chunk(chunkSize);
	}

	@Override
	public <I, O> SimpleStepBuilder<I, O> chunk(CompletionPolicy completionPolicy) {
		return new CustomSimpleStepBuilder<I, O>(this).chunk(completionPolicy);
	}

}
