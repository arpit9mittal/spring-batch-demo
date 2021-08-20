package a9m.demo.batch.config.writers;

import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import a9m.demo.batch.entity.Person;

@Component
@Qualifier("itemWriter")
public class PersonItemWriter implements ItemWriter<Person> {

	private static final Logger log = LoggerFactory.getLogger(PersonItemWriter.class);
	
	@Override
	public void write(List<? extends Person> items) throws Exception {
		log.info(LocalTime.now()+": WRITER SIZE-> " + items.size() );
		for (Person person : items) {
			log.info(LocalTime.now()+": WRITE -> " + person );
		}
	}
}
