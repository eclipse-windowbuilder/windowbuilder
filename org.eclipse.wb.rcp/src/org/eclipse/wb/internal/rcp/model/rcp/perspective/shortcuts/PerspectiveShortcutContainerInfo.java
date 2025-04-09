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
 * Container for {@link IPageLayout#addPerspectiveShortcut(String)} method.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class PerspectiveShortcutContainerInfo extends AbstractShortcutContainerInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PerspectiveShortcutContainerInfo(PageLayoutInfo page) throws Exception {
		super(page, SWT.VERTICAL);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getPresentationText() {
		return "(perspective shortcuts)";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new {@link PerspectiveShortcutInfo}.
	 *
	 * @return the created {@link PerspectiveShortcutInfo}.
	 */
	public PerspectiveShortcutInfo command_CREATE(String perspectiveId,
			PerspectiveShortcutInfo nextItem) throws Exception {
		return command_CREATE(
				perspectiveId,
				PerspectiveShortcutInfo.class,
				nextItem,
				"addPerspectiveShortcuts",
				"addPerspectiveShortcut");
	}

	/**
	 * Moves existing {@link PerspectiveShortcutInfo}.
	 */
	public void command_MOVE(PerspectiveShortcutInfo item, PerspectiveShortcutInfo nextItem)
			throws Exception {
		command_MOVE(item, nextItem, "addPerspectiveShortcuts");
	}
}
