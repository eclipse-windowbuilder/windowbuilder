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
 *    Marcel du Preez - Nullcheck added to setSelection method
 *******************************************************************************/
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.gef.core.IEditPartViewer;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.RootEditPart;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * @author lobas_av
 * @author mitin_aa
 * @coverage gef.core
 */
public abstract class AbstractEditPartViewer extends org.eclipse.gef.ui.parts.AbstractEditPartViewer implements IEditPartViewer {
	private MenuManager m_contextMenu;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the {@link EditDomain EditDomain} to which this viewer belongs.
	 */
	@Override
	public EditDomain getEditDomain() {
		return (EditDomain) super.getEditDomain();
	}

	/**
	 * Set input model for this viewer.
	 */
	public void setInput(Object model) {
		RootEditPart rootEditPart = getRootEditPart();
		EditPart contentEditPart = getEditPartFactory().createEditPart(rootEditPart, model);
		rootEditPart.setContents(contentEditPart);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public MenuManager getContextMenu() {
		return m_contextMenu;
	}

	@Override
	public void setContextMenu(MenuManager menu) {
		// dispose old menu
		if (m_contextMenu != null && m_contextMenu != menu) {
			m_contextMenu.dispose();
		}
		// remember new
		m_contextMenu = menu;
		// create new menu
		Control control = getControl();
		Menu menuWidget = m_contextMenu.createContextMenu(control);
		if (menuWidget.getShell() == control.getShell()) {
			control.setMenu(menuWidget);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GEF
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public Control createControl(Composite parent) {
		return null;
	}
}