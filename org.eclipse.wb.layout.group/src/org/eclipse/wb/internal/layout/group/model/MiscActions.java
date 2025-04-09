/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.layout.group.model;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.layout.group.Messages;

import org.eclipse.jface.action.IMenuManager;

/**
 * Miscellaneous actions for GroupLayout support.
 *
 * @author mitin_aa
 */
public class MiscActions {
	////////////////////////////////////////////////////////////////////////////
	//
	// Private Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private MiscActions() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public static void fillContextMenu(IGroupLayoutInfo layout,
			AbstractComponentInfo component,
			IMenuManager manager) {
		manager.appendToGroup(
				IContextMenuConstants.GROUP_CONSTRAINTS,
				new SetDefaultSizeAction(layout, component));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Impl
	//
	////////////////////////////////////////////////////////////////////////////
	private static void action_setComponentDefaultSize(IGroupLayoutInfo layout, JavaInfo component)
			throws Exception {
		String id = ObjectInfoUtils.getId(component);
		layout.getLayoutDesigner().setDefaultSize(id);
		layout.saveLayout();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Action
	//
	////////////////////////////////////////////////////////////////////////////
	private final static class SetDefaultSizeAction extends ObjectInfoAction {
		private final AbstractComponentInfo m_component;
		private final IGroupLayoutInfo m_layout;

		private SetDefaultSizeAction(IGroupLayoutInfo layout, AbstractComponentInfo component) {
			super(component, Messages.MiscActions_setDefaultSize);
			m_layout = layout;
			m_component = component;
			setEnabled(component != null);
		}

		@Override
		protected void runEx() throws Exception {
			action_setComponentDefaultSize(m_layout, m_component);
		}
	}
}
