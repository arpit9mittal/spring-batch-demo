/**
 * 
 */
package a9m.demo.batch.framework;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 
 * Convenient factory for a {@link StepBuilder} which sets the
 * {@link JobRepository} and {@link PlatformTransactionManager} automatically.
 * 
 * @author s2565087
 *
 */
@Component
public class CustomStepBuilderFactory {

	private JobRepository jobRepository;

	private PlatformTransactionManager transactionManager;

	@Autowired
	public CustomStepBuilderFactory(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
	}

	/**
	 * Creates a step builder and initializes its job repository and transaction
	 * manager. Note that if the builder is used to create a &#64;Bean definition
	 * then the name of the step and the bean name might be different.
	 * 
	 * @param name the name of the step
	 * @return a step builder
	 */
	public StepBuilder get(String name) {
		StepBuilder builder = new CustomStepBuilder(name).repository(jobRepository).transactionManager(transactionManager);
		return builder;
	}

}