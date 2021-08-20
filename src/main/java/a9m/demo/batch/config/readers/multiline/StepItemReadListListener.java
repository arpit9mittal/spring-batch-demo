package a9m.demo.batch.config.readers.multiline;

import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("itemListReadListener")
public class StepItemReadListListener<I> implements ItemReadListener<List<I>> {
	
	private static final Logger log = LoggerFactory.getLogger(StepItemReadListListener.class);

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
	public void afterRead(List<I> items) {
		log.info("==> Reader After -> " + LocalTime.now() + " ("+items.size()+")");
		for (I i : items) {
			log.info("		==> " + i);
		}
		
	}

}
