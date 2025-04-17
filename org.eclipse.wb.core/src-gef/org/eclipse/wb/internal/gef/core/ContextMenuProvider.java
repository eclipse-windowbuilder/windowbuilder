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
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.gef.core.IEditPartViewer;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Extends {@link MenuManager} to allow populating the menu directly from the manager itself.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public abstract class ContextMenuProvider extends MenuManager {
	protected final IEditPartViewer m_viewer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ContextMenuProvider(IEditPartViewer viewer) {
		m_viewer = viewer;
		setRemoveAllWhenShown(true);
		addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				// dispose items to avoid their caching
				for (MenuItem item : getMenu().getItems()) {
					item.dispose();
				}
				// apply new items
				buildContextMenu();
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context Menu
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Called when the menu is about to show. Subclasses must implement this method to populate the
	 * menu each time it is shown.
	 */
	protected abstract void buildContextMenu();
}