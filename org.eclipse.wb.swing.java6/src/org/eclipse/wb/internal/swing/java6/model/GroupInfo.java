/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
