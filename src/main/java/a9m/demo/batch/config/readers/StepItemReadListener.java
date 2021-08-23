package a9m.demo.batch.config.readers;

import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("simpleStepItemReadListner")
public class StepItemReadListener<T> implements ItemReadListener<T> {
	
	private static final Logger log = LoggerFactory.getLogger(StepItemReadListener.class);

	@Override
	public void beforeRead() {
		log.info("==> Reader Before -> " + LocalTime.now());
		
	}

	@Override
	public void afterRead(T item) {
		log.info("==> Reader After -> " + LocalTime.now() + " ("+item+")");
		
	}

	@Override
	public void onReadError(Exception ex) {
		log.error("Encountered error on read", ex);
		// throw RuntimeException to stop the batch;
		// https://docs.spring.io/spring-batch/docs/current/reference/html/common-patterns.html
	}

}
