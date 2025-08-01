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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * Implementation {@link ICheckboxViewerWrapper} for {@link CheckboxTreeViewer}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class CheckboxTreeViewerWrapper implements ICheckboxViewerWrapper {
	private final CheckboxTreeViewer m_viewer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CheckboxTreeViewerWrapper(CheckboxTreeViewer viewer) {
		m_viewer = viewer;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ICheckboxViewerWrapper
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public StructuredViewer getViewer() {
		return m_viewer;
	}

	@Override
	public ICheckable getCheckable() {
		return m_viewer;
	}

	@Override
	public Object[] getCheckedElements() {
		return m_viewer.getCheckedElements();
	}

	@Override
	public void setCheckedElements(Object[] elements) {
		m_viewer.setCheckedElements(elements);
	}

	@Override
	@Deprecated
	public void setAllChecked(boolean state) {
		m_viewer.setAllChecked(state);
	}
}