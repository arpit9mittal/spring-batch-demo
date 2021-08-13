package my.demo.batch;

import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.stereotype.Component;

import my.demo.batch.multiline.Trade;

@Component
public class StepItemReadListener implements ItemReadListener<Trade> {
	
	private static final Logger log = LoggerFactory.getLogger(StepItemReadListener.class);

	@Override
	public void beforeRead() {
		log.info("==> Reader Before -> " + LocalTime.now());
		
	}

	@Override
	public void onReadError(Exception ex) {
		log.error("Encountered error on read", ex);
		// throw RuntimeException to stop the batch;
		// https://docs.spring.io/spring-batch/docs/current/reference/html/common-patterns.html
	}

	@Override
	public void afterRead(Trade item) {
		log.info("==> Reader After -> " + LocalTime.now() + " ("+item+")");
	}

}
