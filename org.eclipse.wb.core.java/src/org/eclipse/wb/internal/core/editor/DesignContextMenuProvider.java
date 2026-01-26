/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    Marcel du Preez - adjusted buildContextMenu method to include version of context menu for Basic UI
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.editor.constants.IEditorPreferenceConstants;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.editor.actions.DesignPageActions;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.gef.core.ContextMenuProvider;
import org.eclipse.wb.internal.gef.core.MultiSelectionContextMenuProvider;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ContextMenuProvider} for Designer.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public final class DesignContextMenuProvider extends MultiSelectionContextMenuProvider
implements
IContextMenuConstants {
	////////////////////////////////////////////////////////////////////////////
	//
	// Groups
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds standard groups into given {@link IMenuManager}.
	 */
	public static void addGroups(IMenuManager manager) {
		manager.add(new Separator(IContextMenuConstants.GROUP_TOP));
		manager.add(new Separator(IContextMenuConstants.GROUP_EDIT));
		manager.add(new Separator(IContextMenuConstants.GROUP_EDIT2));
		manager.add(new Separator(IContextMenuConstants.GROUP_EVENTS));
		manager.add(new Separator(IContextMenuConstants.GROUP_EVENTS2));
		manager.add(new Separator(IContextMenuConstants.GROUP_LAYOUT));
		manager.add(new Separator(IContextMenuConstants.GROUP_CONSTRAINTS));
		manager.add(new Separator(IContextMenuConstants.GROUP_INHERITANCE));
		manager.add(new Separator(IContextMenuConstants.GROUP_ADDITIONAL));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private final DesignPageActions m_pageActions;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DesignContextMenuProvider(EditPartViewer viewer, DesignPageActions pageActions) {
		super(viewer);
		m_pageActions = pageActions;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MultiSelectionContextMenuProvider
	//
	////////////////////////////////////////////////////////////////////////////
	private List<ObjectInfo> m_selectedObjects;

	@Override
	protected void preprocessSelection(List<? extends EditPart> editParts) {
		super.preprocessSelection(editParts);
		// prepare selected ObjectInfo's
		m_selectedObjects = new ArrayList<>();
		for (EditPart editPart : editParts) {
			if (editPart.getModel() instanceof ObjectInfo) {
				m_selectedObjects.add((ObjectInfo) editPart.getModel());
			}
		}
	}

	@Override
	protected void buildContextMenu(final EditPart editPart, final IMenuManager manager) {
		addGroups(manager);
		boolean wbBasic = InstanceScope.INSTANCE.getNode(
				IEditorPreferenceConstants.WB_BASIC_UI_PREFERENCE_NODE).getBoolean(
						IEditorPreferenceConstants.WB_BASIC_UI,
						false);
		// edit
		{
			manager.appendToGroup(IContextMenuConstants.GROUP_EDIT, m_pageActions.getCutAction());
			manager.appendToGroup(IContextMenuConstants.GROUP_EDIT, m_pageActions.getCopyAction());
			manager.appendToGroup(IContextMenuConstants.GROUP_EDIT, m_pageActions.getPasteAction());
			manager.appendToGroup(IContextMenuConstants.GROUP_EDIT, m_pageActions.getDeleteAction());
		}
		// edit2
		{
			if (!wbBasic) {
				manager.appendToGroup(IContextMenuConstants.GROUP_EDIT2, m_pageActions.getTestAction());
				manager.appendToGroup(IContextMenuConstants.GROUP_EDIT2, m_pageActions.getRefreshAction());
			}
		}
		// send notification
		if (!wbBasic) {
			if (editPart.getModel() instanceof ObjectInfo) {
				ExecutionUtils.runLog(new RunnableEx() {
					@Override
					public void run() throws Exception {
						ObjectInfo object = (ObjectInfo) editPart.getModel();
						object.getBroadcastObject().addContextMenu(m_selectedObjects, object, manager);
					}
				});
			}
		}
	}
}
