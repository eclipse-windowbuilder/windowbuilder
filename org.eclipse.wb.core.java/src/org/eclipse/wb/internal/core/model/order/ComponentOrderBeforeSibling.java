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
package org.eclipse.wb.internal.core.model.order;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * {@link ComponentOrder} for component that should be added before siblings of some type.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ComponentOrderBeforeSibling extends ComponentOrder {
	private final String m_nextComponentClass;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentOrderBeforeSibling(String nextComponentClass) {
		m_nextComponentClass = nextComponentClass;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ComponentOrder
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public JavaInfo getNextComponent_whenLast(JavaInfo component, JavaInfo container)
			throws Exception {
		for (JavaInfo sibling : container.getChildrenJava()) {
			if (isNextSibling(sibling)) {
				return sibling;
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean isNextSibling(JavaInfo sibling) {
		return ReflectionUtils.isSuccessorOf(
				sibling.getDescription().getComponentClass(),
				m_nextComponentClass);
	}
}
