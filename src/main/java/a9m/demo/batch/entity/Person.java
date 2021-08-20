package a9m.demo.batch.entity;

public class Person {

	private int id;
	private String lastName;
	private String firstName;

	public Person() {
	}

	public Person(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public Person(int id, String lastName, String firstName) {
		super();
		this.id = id;
		this.lastName = lastName;
		this.firstName = firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		if(id==0)
			return "firstName: " + firstName + ", lastName: " + lastName;
		else
			return "ID: " + id + " firstName: " + firstName + ", lastName: " + lastName;
	}

}
