/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.swing.java6.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for GroupLayout container elements.
 *
 * @author mitin_aa
 * @coverage swing.model.layout.group
 */
public abstract class GroupInfo extends SpringInfo {
	protected List<SpringInfo> m_children = new ArrayList<>();

	/**
	 * Adds a child spring.
	 */
	public void add(SpringInfo child) {
		m_children.add(child);
		child.setParent(this);
	}
}
