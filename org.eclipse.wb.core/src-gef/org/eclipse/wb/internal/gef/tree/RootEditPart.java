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
package org.eclipse.wb.internal.gef.tree;

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.tree.TreeEditPart;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 *
 * @author lobas_av
 * @coverage gef.tree
 */
public class RootEditPart extends TreeEditPart implements org.eclipse.gef.RootEditPart {
	private IEditPartViewer m_viewer;
	private TreeEditPart m_contentEditPart;

	////////////////////////////////////////////////////////////////////////////
	//
	// EditPart
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the root's {@link EditPartViewer}.
	 */
	@Override
	public IEditPartViewer getViewer() {
		return m_viewer;
	}

	@Override
	public void setViewer(EditPartViewer viewer) {
		m_viewer = (IEditPartViewer) viewer;
	}

	@Override
	protected void addChildVisual(org.eclipse.gef.EditPart childPart, int index) {
		m_contentEditPart.setWidget(new TreeItem(getTreeControl(), SWT.NONE));
	}

	private Tree getTreeControl() {
		return (Tree) m_viewer.getControl();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IRootEditPart
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the <i>content</i> {@link EditPart}.
	 */
	@Override
	public EditPart getContents() {
		return m_contentEditPart;
	}

	/**
	 * Sets the <i>content</i> {@link EditPart}. A IRootEditPart only has a single child, called its
	 * <i>contents</i>.
	 */
	@Override
	public void setContents(org.eclipse.gef.EditPart contentEditPart) {
		if (m_contentEditPart != null) {
			// remove content
			removeChild(m_contentEditPart);
		}
		//
		m_contentEditPart = (TreeEditPart) contentEditPart;
		//
		if (m_contentEditPart != null) {
			addChild(m_contentEditPart, -1);
		}
	}
}