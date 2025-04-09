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
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.gef.policy.jface.action.ActionDropTool;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Implementation of {@link EntryInfo} that is contributed to palette for each {@link IAction}
 * instance existing on form, so that it can be selected and used on some
 * {@link IContributionManager}.
 *
 * @author scheglov_ke
 * @coverage rcp.editor.palette
 */
public final class ActionUseEntryInfo extends ToolEntryInfo {
	private static final ImageDescriptor ICON = Activator.getImageDescriptor("info/Action/action.gif");
	private final ActionInfo m_action;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ActionUseEntryInfo(ActionInfo action) throws Exception {
		m_action = action;
		setId(ObjectUtils.identityToString(action));
		setName(action.getVariableSupport().getComponentName());
		setDescription(PaletteMessages.ActionUseEntryInfo_description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon() {
		return ExecutionUtils.runObjectLog(() -> m_action.getPresentation().getIcon(), ICON);
	}

	@Override
	public Tool createTool() throws Exception {
		return new ActionDropTool(m_action);
	}
}
