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
package org.eclipse.wb.core.editor.palette.model.entry;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.model.entry.IDefaultEntryInfo;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Implementation of {@link EntryInfo} that activates {@link SelectionTool}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public class SelectionToolEntryInfo extends ToolEntryInfo implements IDefaultEntryInfo {
	private static final ImageDescriptor ICON = DesignerPlugin.getImageDescriptor("palette/SelectionTool.gif");
	private final SelectionTool m_selectionTool = new SelectionTool();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SelectionToolEntryInfo() {
		setName(Messages.SelectionToolEntryInfo_name);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon() {
		return ICON;
	}

	@Override
	public Tool createTool() throws Exception {
		return m_selectionTool;
	}
}
