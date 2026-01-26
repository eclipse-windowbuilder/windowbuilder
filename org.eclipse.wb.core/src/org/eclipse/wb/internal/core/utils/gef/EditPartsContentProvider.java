/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.utils.gef;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ITreeContentProvider} for GEF {@link EditPartViewer}.
 *
 * @author scheglov_ke
 * @coverage gef.core
 */
public final class EditPartsContentProvider implements ITreeContentProvider {
	private final EditPartViewer m_viewer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EditPartsContentProvider(EditPartViewer viewer) {
		m_viewer = viewer;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IStructuredContentProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object[] getElements(Object inputElement) {
		Object input = m_viewer.getRootEditPart().getContents().getModel();
		return new Object[]{input};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ITreeContentProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean hasChildren(Object parentElement) {
		return getChildren(parentElement).length != 0;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		EditPart parentEditPart = m_viewer.getEditPartRegistry().get(parentElement);
		if (parentEditPart != null) {
			List<Object> children = new ArrayList<>();
			for (EditPart editPart : parentEditPart.getChildren()) {
				children.add(editPart.getModel());
			}
			return children.toArray();
		}
		return ArrayUtils.EMPTY_OBJECT_ARRAY;
	}

	@Override
	public Object getParent(Object element) {
		EditPart editPart = m_viewer.getEditPartRegistry().get(element);
		if (editPart != null) {
			return editPart.getParent().getModel();
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IContentProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
