package jphonebook.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 * 
 */
public class PhoneGroups extends AbstractModelObject {
	private List<PhoneGroup> m_groups = new ArrayList<PhoneGroup>();

	public void addGroup(PhoneGroup group) {
		List<PhoneGroup> oldValue = m_groups;
		m_groups = new ArrayList<PhoneGroup>(m_groups);
		m_groups.add(group);
		firePropertyChange("groups", oldValue, m_groups);
	}

	public void removeGroup(PhoneGroup group) {
		List<PhoneGroup> oldValue = m_groups;
		m_groups = new ArrayList<PhoneGroup>(m_groups);
		m_groups.remove(group);
		firePropertyChange("groups", oldValue, m_groups);
	}

	public List<PhoneGroup> getGroups() {
		return m_groups;
	}
}