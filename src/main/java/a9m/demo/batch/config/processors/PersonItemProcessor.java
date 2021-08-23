package a9m.demo.batch.config.processors;

import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import a9m.demo.batch.entity.Person;

@Component
@Qualifier("processor")
public class PersonItemProcessor implements ItemProcessor<Person, Person> {

	private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

	@Override
	public Person process(final Person person) throws Exception {
		final String firstName = person.getFirstName().toUpperCase();
		final String lastName = person.getLastName().toUpperCase();

		final Person transformedPerson = new Person(firstName, lastName);
		log.info(LocalTime.now()+": IN Processor ("+ transformedPerson + ")");
		Thread.sleep(2000);
		log.info(LocalTime.now()+": OUT Processor ("+ transformedPerson + ")");

		return transformedPerson;
	}

}
