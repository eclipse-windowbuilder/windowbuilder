package jphonebook.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 * 
 */
public class PhoneGroup extends AbstractModelObject {
	private List<Person> m_persons = new ArrayList<Person>();
	private String m_name;

	public PhoneGroup() {
	}

	public PhoneGroup(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		String oldValue = m_name;
		m_name = name;
		firePropertyChange("name", oldValue, m_name);
	}

	public void addPerson(Person person) {
		List<Person> oldValue = m_persons;
		m_persons = new ArrayList<Person>(m_persons);
		m_persons.add(person);
		firePropertyChange("persons", oldValue, m_persons);
		firePropertyChange("personCount", oldValue.size(), m_persons.size());
	}

	public void removePerson(Person person) {
		List<Person> oldValue = m_persons;
		m_persons = new ArrayList<Person>(m_persons);
		m_persons.remove(person);
		firePropertyChange("persons", oldValue, m_persons);
		firePropertyChange("personCount", oldValue.size(), m_persons.size());
	}

	public List<Person> getPersons() {
		return m_persons;
	}
	
	public int getPersonCount() {
		return m_persons.size();
	}
}