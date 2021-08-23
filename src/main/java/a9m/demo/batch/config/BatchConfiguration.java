package a9m.demo.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchConfiguration {

	private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);
	
	@Autowired
	public JobLauncher jobLauncher ;
	
    @Bean
    public TaskExecutor taskExecutor(
    		@Value("${batch.task.executor.pool.size.core}") int poolCoreSize, 
    		@Value("${batch.task.executor.pool.size.max}") int poolMaxSize, 
    		@Value("${batch.task.executor.queue.capacity}") int queueCapacity,
    		@Value("${batch.task.executor.saturation.policy.wait}") int executorTaskWaitOnQueue,
    		@Value("${batch.task.executor.saturation.policy.retry}") int executorTaskRetryCount) {

    	ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("MultiThreaded-");

        executor.setCorePoolSize(poolCoreSize);
        executor.setMaxPoolSize(poolMaxSize);
        executor.setQueueCapacity(queueCapacity);
        
        executor.setRejectedExecutionHandler(new WaitRetryThenCallerRunsPolicy("BatchTaskExecutor", executorTaskWaitOnQueue, executorTaskRetryCount));
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        return executor;
    }
    
//    Available Jobs to play with: 
//    1. normalJob
//    2. asyncJob
//    3. multiThreadedJob
//    4. asyncMultiRecordJob
//    5. multilineJob
    
    
	@Bean
	public JobExecution run(
			JobLauncher jobLauncher, 
			@Qualifier("asyncMultiRecordJob") Job job,
			@Value("${batch.file.one.in}") String sampleInputFile, 
    		@Value("${batch.file.one.col}") String sampleInputFileColumn, 
    		@Value("${batch.file.two.in}") String multiLineInputFile,
    		@Value("${batch.file.two.out}") String multiLineOutputFile
			) {
		
	    JobExecution jobExecution = null;
	    
	    String inputFileName = null, 
	    		inputFileColumn = null, 
	    		outputFileName = null;
	    
	    
	    switch (job.getName()) {
		case "multilineJob":
			inputFileName = multiLineInputFile ;
			outputFileName = multiLineOutputFile ;
			break;

		default:
			inputFileName = sampleInputFile ;
			inputFileColumn = sampleInputFileColumn ;
			break;
		}
	    
	    try {
	        JobParameters jobParameters = new JobParametersBuilder()
	                .addLong("time", System.currentTimeMillis())
	                .addString("in.file.name", inputFileName)
	                .addString("out.file.name", outputFileName)
	                .addString("in.file.col", inputFileColumn)
	                .toJobParameters();

	        jobExecution = jobLauncher.run(job, jobParameters);
	        log.info("Exit Status : " + jobExecution.getStatus());
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return jobExecution;
	}
}
