/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.rcp.databinding;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;

import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;

/**
 * This class may be freely distributed as part of any application or plugin.
 * 
 * @author lobas_av
 */
public final class TreeBeanAdvisor extends TreeStructureAdvisor {
	private final Class<?> m_beanClass;
	private final Method m_getParentMethod;
	private final Method m_getChildrenMethod;
	private final Method m_hasChildrenMethod;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TreeBeanAdvisor(Class<?> beanClass,
			String parentProperty,
			String childrenProperty,
			String hasChildrenProperty) {
		m_beanClass = beanClass;
		m_getParentMethod = Utils.getMethod(m_beanClass, parentProperty);
		m_getChildrenMethod = Utils.getMethod(m_beanClass, childrenProperty);
		m_hasChildrenMethod = Utils.getMethod(m_beanClass, hasChildrenProperty);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// TreeStructureAdvisor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getParent(Object element) {
		return Utils.invokeMethod(m_getParentMethod, m_beanClass, element);
	}
	@Override
	public Boolean hasChildren(Object element) {
		if (m_hasChildrenMethod != null) {
			return (Boolean) Utils.invokeMethod(m_hasChildrenMethod, m_beanClass, element);
		}
		if (m_getChildrenMethod != null) {
			Object children = Utils.invokeMethod(m_getChildrenMethod, m_beanClass, element);
			if (children == null) {
				return Boolean.FALSE;
			}
			if (children.getClass().isArray()) {
				return Array.getLength(children) > 0;
			}
			if (children instanceof Collection<?>) {
				Collection<?> collection = (Collection<?>) children;
				return !collection.isEmpty();
			}
		}
		return null;
	}
}