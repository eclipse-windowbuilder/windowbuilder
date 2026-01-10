/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
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