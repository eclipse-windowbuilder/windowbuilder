/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.editor.palette.model.entry;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.gef.tools.TabOrderTool;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

/**
 * Implementation of {@link EntryInfo} that activates {@link TabOrderTool}.
 *
 * @author lobas_av
 * @coverage core.editor.palette
 */
public final class TabOrderToolEntryInfo extends ToolEntryInfo {
	private static final ImageDescriptor ICON = DesignerPlugin.getImageDescriptor("palette/tab_order.gif");

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TabOrderToolEntryInfo() {
		setName(Messages.TabOrderToolEntryInfo_name);
		setDescription(Messages.TabOrderToolEntryInfo_description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Tool createTool() throws Exception {
		List<? extends EditPart> selectedParts = m_editPartViewer.getSelectedEditParts();
		if (selectedParts.size() == 1) {
			EditPart editPart = selectedParts.get(0);
			if (TabOrderTool.hasContainerRole(editPart)) {
				return new TabOrderTool(editPart);
			}
		}
		return null;
	}

	@Override
	public ImageDescriptor getIcon() {
		return ICON;
	}
}