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
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.gef.policy.jface.action.ActionDropTool;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;
import org.eclipse.wb.internal.rcp.model.rcp.ActionFactoryCreationSupport;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import java.util.Map;
import java.util.TreeMap;

/**
 * {@link EntryInfo} that drop new {@link ActionInfo} from {@link ActionFactory} using
 * {@link ActionDropTool}.
 *
 * @author scheglov_ke
 * @coverage rcp.editor.palette
 */
public final class ActionFactoryNewEntryInfo extends ToolEntryInfo {
	private static final Map<String, ImageDescriptor> m_namedIcons = new TreeMap<>();
	private final String m_name;
	private final ImageDescriptor m_icon;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ActionFactoryNewEntryInfo(String name) throws Exception {
		m_name = name;
		setId("org.eclipse.ui.actions.ActionFactory." + name);
		// prepare IWorkbenchAction
		IWorkbenchAction action;
		{
			ActionFactory actionFactory =
					(ActionFactory) ReflectionUtils.getFieldObject(ActionFactory.class, name);
			action = actionFactory.create(DesignerPlugin.getActiveWorkbenchWindow());
		}
		// use IWorkbenchAction to configure this entry
		try {
			setName(action.getText());
			setDescription(action.getDescription());
			// icon
			{
				ImageDescriptor icon = m_namedIcons.get(m_name);
				if (icon == null) {
					if (action.getImageDescriptor() != null) {
						icon = action.getImageDescriptor();
					} else {
						icon = ActionFactoryCreationSupport.DEFAULT_ICON;
					}
					m_namedIcons.put(m_name, icon);
				}
				m_icon = icon;
			}
		} finally {
			action.dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon() {
		return m_icon;
	}

	@Override
	public Tool createTool() throws Exception {
		ActionInfo action = ActionFactoryCreationSupport.createNew(m_rootJavaInfo, m_name);
		return new ActionDropTool(action);
	}
}
