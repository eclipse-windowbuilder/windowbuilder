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
package org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts;

import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IPageLayout;

/**
 * Container for {@link IPageLayout#addFastView(String)} method.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class FastViewContainerInfo extends AbstractShortcutContainerInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FastViewContainerInfo(PageLayoutInfo page) throws Exception {
		super(page, SWT.HORIZONTAL);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getPresentationText() {
		return "(fast views)";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new {@link FastViewInfo}.
	 *
	 * @return the created {@link FastViewInfo}.
	 */
	public FastViewInfo command_CREATE(String viewId, FastViewInfo nextItem) throws Exception {
		return command_CREATE(viewId, FastViewInfo.class, nextItem, "addFastViews", "addFastView");
	}

	/**
	 * Moves existing {@link FastViewInfo}.
	 */
	public void command_MOVE(FastViewInfo item, FastViewInfo nextItem) throws Exception {
		command_MOVE(item, nextItem, "addFastViews");
	}
}
