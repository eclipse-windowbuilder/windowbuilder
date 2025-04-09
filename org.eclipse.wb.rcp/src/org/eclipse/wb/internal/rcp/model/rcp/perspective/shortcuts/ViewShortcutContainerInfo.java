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
package org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts;

import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IPageLayout;

/**
 * Container for {@link IPageLayout#addShowViewShortcut(String)} method.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class ViewShortcutContainerInfo extends AbstractShortcutContainerInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewShortcutContainerInfo(PageLayoutInfo page) throws Exception {
		super(page, SWT.VERTICAL);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getPresentationText() {
		return "(view shortcuts)";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new {@link ViewShortcutInfo}.
	 *
	 * @return the created {@link ViewShortcutInfo}.
	 */
	public ViewShortcutInfo command_CREATE(String viewId, ViewShortcutInfo nextItem) throws Exception {
		return command_CREATE(
				viewId,
				ViewShortcutInfo.class,
				nextItem,
				"addViewShortcuts",
				"addShowViewShortcut");
	}

	/**
	 * Moves existing {@link ViewShortcutInfo}.
	 */
	public void command_MOVE(ViewShortcutInfo item, ViewShortcutInfo nextItem) throws Exception {
		command_MOVE(item, nextItem, "addViewShortcuts");
	}
}
