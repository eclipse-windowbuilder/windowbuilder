/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.gef.policy.jface.DialogButtonDropTool;

import org.eclipse.jface.resource.ImageDescriptor;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JToolBar;

/**
 * Implementation of {@link EntryInfo} that is contributed to palette for each {@link Action}
 * instance existing on form, so that it can be selected and used for example for other
 * {@link AbstractButton}, or added on {@link JMenu} or {@link JToolBar}.
 *
 * @author scheglov_ke
 * @coverage rcp.editor.palette
 */
public final class DialogButtonEntryInfo extends ToolEntryInfo {
	private static final ImageDescriptor ICON = Activator.getImageDescriptor("info/Dialog/button.gif");

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DialogButtonEntryInfo() throws Exception {
		setId(getClass().getName());
		setName(PaletteMessages.DialogButtonEntryInfo_name);
		setDescription(PaletteMessages.DialogButtonEntryInfo_description);
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
		return new DialogButtonDropTool();
	}
}
