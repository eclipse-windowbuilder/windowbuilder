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
package org.eclipse.wb.internal.swing.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;

import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.lang3.ObjectUtils;

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
 * @coverage swing.editor.palette
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
		setName(action.getVariableSupport().getName());
		setDescription(ModelMessages.ActionUseEntryInfo_description);
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
		return createActionTool(m_action);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Tool} for dropping {@link ActionInfo}.
	 */
	static Tool createActionTool(final ActionInfo action) {
		// prepare factory
		ICreationFactory factory = new ICreationFactory() {
			@Override
			public void activate() throws Exception {
			}

			@Override
			public Object getNewObject() {
				return action;
			}
		};
		// return tool
		return new CreationTool(factory) {
			@Override
			protected void selectAddedObjects() {
			}
		};
	}
}
