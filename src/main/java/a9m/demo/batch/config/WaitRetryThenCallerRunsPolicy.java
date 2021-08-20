package a9m.demo.batch.config;

import java.time.LocalTime;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author c51680
 *
 */
public class WaitRetryThenCallerRunsPolicy implements RejectedExecutionHandler {

    private static final Logger log = LoggerFactory.getLogger(WaitRetryThenCallerRunsPolicy.class);

    /**
     * Number of re-try on queue wait time out. Defaulted to 0
     */
    private int executorTaskRetryCount = 0;

    /**
     * time in milli seconds to wait on queue, default to INT MAX
     */
    private int executorTaskWaitOnQueueMs = Integer.MAX_VALUE;

    /**
     * ThreadPool Name
     */
    private String threadPoolName;

    /**
     * Default constructor
     */
    public WaitRetryThenCallerRunsPolicy() {
        super();
        this.threadPoolName = "Not Defined";
    }

    /**
     * @param threadPoolName
     *            Name of the ThreadPool
     * @param executorTaskWaitOnQueueMs
     *            time in milli seconds
     * @param executorTaskRetryCount
     *            number of re-try incase the queue is full and wait time outs
     */
    public WaitRetryThenCallerRunsPolicy(String threadPoolName, int executorTaskWaitOnQueueMs, int executorTaskRetryCount) {
        this.threadPoolName = threadPoolName;
        this.executorTaskRetryCount = executorTaskRetryCount;
        this.executorTaskWaitOnQueueMs = executorTaskWaitOnQueueMs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.RejectedExecutionHandler#rejectedExecution(java.lang.Runnable,
     * java.util.concurrent.ThreadPoolExecutor)
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        submit(0, r, executor);
    }

    /**
     * @param count
     * @param r
     * @param executor
     */
    private void submit(int count, Runnable r, ThreadPoolExecutor executor) {
        boolean isSubmitted = false;

        try {
            isSubmitted = executor.getQueue().offer(r, executorTaskWaitOnQueueMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Submitting RejectedExecution got interrupted in thread pool " + threadPoolName, e);
        }

        if (!isSubmitted) {
            // do not retry more than desired times.
            count++;
            if (count <= executorTaskRetryCount) {
                log.debug(".. Retry attempt - {}", count);
                submit(count, r, executor);
            } else {
                log.warn("Unable to submit after '{}' re-try in thread pool '{}'", executorTaskRetryCount, threadPoolName);
                
                if (!executor.isShutdown()) {
                	log.info("Runing the task in main thread at {}'", LocalTime.now());
                    r.run();
                    log.info("Execution completed in main thread at {}'", LocalTime.now());
                }
            }
        }
    }

    /**
     * @return the executorTaskWaitOnQueueMs
     */
    public int getExecutorTaskWaitOnQueueMs() {
        return executorTaskWaitOnQueueMs;
    }

    /**
     * @return the executorTaskRetryCount
     */
    public int getExecutorTaskRetryCount() {
        return executorTaskRetryCount;
    }

}

