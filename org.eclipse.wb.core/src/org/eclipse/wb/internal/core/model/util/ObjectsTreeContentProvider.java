/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import java.util.function.Predicate;

/**
 * Implementation of {@link ITreeContentProvider} for {@link ObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class ObjectsTreeContentProvider implements ITreeContentProvider {
	private final Predicate<ObjectInfo> m_predicate;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ObjectsTreeContentProvider(Predicate<ObjectInfo> predicate) {
		m_predicate = predicate;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Input
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		} else {
			return getChildren(inputElement);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object[] getChildren(Object parentElement) {
		return ((ObjectInfo) parentElement).getChildren() //
				.stream() //
				.filter(m_predicate) //
				.toArray();
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length != 0;
	}

	@Override
	public Object getParent(Object element) {
		return ((ObjectInfo) element).getParent();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {
	}
}
