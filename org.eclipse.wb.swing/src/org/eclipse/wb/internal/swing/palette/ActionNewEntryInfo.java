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
package org.eclipse.wb.internal.swing.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;

import org.eclipse.jface.resource.ImageDescriptor;

import javax.swing.Action;

/**
 * Implementation of {@link EntryInfo} that allows user to create new inner {@link Action} and drop
 * it.
 *
 * @author scheglov_ke
 * @coverage swing.editor.palette
 */
public final class ActionNewEntryInfo extends ToolEntryInfo {
	private static final ImageDescriptor ICON = Activator.getImageDescriptor("info/Action/action_new.gif");

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ActionNewEntryInfo() {
		setName(PaletteMessages.ActionNewEntryInfo_name);
		setDescription(PaletteMessages.ActionNewEntryInfo_description);
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
		ActionInfo action = ActionInfo.createInner(m_editor);
		return ActionUseEntryInfo.createActionTool(action);
	}
}
