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
package phonebook.model;

import java.util.ArrayList;
import java.util.List;

public class PhoneGroups extends AbstractModelObject {
	private final List<PhoneGroup> m_groups = new ArrayList<>();

	public void addGroup(PhoneGroup group) {
		m_groups.add(group);
		firePropertyChange("groups", null, m_groups);
	}

	public void removeGroup(PhoneGroup group) {
		m_groups.remove(group);
		firePropertyChange("groups", null, m_groups);
	}

	public List<PhoneGroup> getGroups() {
		return m_groups;
	}
}