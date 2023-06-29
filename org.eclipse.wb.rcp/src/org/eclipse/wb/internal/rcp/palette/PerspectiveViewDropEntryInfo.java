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
package org.eclipse.wb.internal.rcp.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.ViewDropTool;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;

import org.eclipse.swt.graphics.Image;

/**
 * {@link EntryInfo} that allows user to drop new view on {@link PageLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.editor.palette
 */
public final class PerspectiveViewDropEntryInfo extends ToolEntryInfo {
	private final ViewInfo m_view;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PerspectiveViewDropEntryInfo(ViewInfo view) {
		m_view = view;
		setId(view.getId());
		setName(m_view.getName());
		setDescription(m_view.getId() + "\n" + m_view.getClassName());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Image getIcon() {
		return m_view.getIcon();
	}

	@Override
	public Tool createTool() throws Exception {
		return new ViewDropTool(m_view);
	}
}
