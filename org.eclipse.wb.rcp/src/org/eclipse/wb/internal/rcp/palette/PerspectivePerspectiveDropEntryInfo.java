/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.PerspectiveDropTool;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.PerspectiveInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link EntryInfo} that allows user to drop new perspective on {@link PageLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.editor.palette
 */
public final class PerspectivePerspectiveDropEntryInfo extends ToolEntryInfo {
	private final PerspectiveInfo m_perspective;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PerspectivePerspectiveDropEntryInfo(PerspectiveInfo perspective) {
		m_perspective = perspective;
		setId(perspective.getId());
		setName(m_perspective.getName());
		setDescription(m_perspective.getId() + "\n" + m_perspective.getClassName());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon() {
		return m_perspective.getIcon();
	}

	@Override
	public Tool createTool() throws Exception {
		return new PerspectiveDropTool(m_perspective);
	}
}
