package a9m.demo.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * -Dspring.batch.job.names=asyncJob
 *
 */
@SpringBootApplication
@EnableBatchProcessing
public class BatchProcessingApplication {
	
	public static void main(String[] args) throws Exception {
		System.exit(SpringApplication.exit(SpringApplication.run(BatchProcessingApplication.class, args)));
		
	}
}
