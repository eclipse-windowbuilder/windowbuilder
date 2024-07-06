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
package org.eclipse.wb.rcp.databinding;

import java.lang.reflect.Array;
import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;

/**
 * This class may be freely distributed as part of any application or plugin.
 * 
 * @author lobas_av
 */
public final class EMFTreeBeanAdvisor extends TreeStructureAdvisor {
	private final EStructuralFeature m_parentProperty;
	private final EStructuralFeature m_childrenProperty;
	private final EStructuralFeature m_hasChildrenProperty;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EMFTreeBeanAdvisor(EStructuralFeature parentProperty,
			EStructuralFeature childrenProperty,
			EStructuralFeature hasChildrenProperty) {
		m_parentProperty = parentProperty;
		m_childrenProperty = childrenProperty;
		m_hasChildrenProperty = hasChildrenProperty;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// TreeStructureAdvisor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getParent(Object element) {
		if (m_parentProperty != null && element instanceof EObject) {
			EObject eObject = (EObject) element;
			return eObject.eGet(m_parentProperty);
		}
		return null;
	}
	@Override
	public Boolean hasChildren(Object element) {
		if (element instanceof EObject) {
			EObject eObject = (EObject) element;
			if (m_hasChildrenProperty != null) {
				return (Boolean) eObject.eGet(m_hasChildrenProperty);
			}
			if (m_childrenProperty != null) {
				Object children = eObject.eGet(m_childrenProperty);
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
		}
		return null;
	}
}