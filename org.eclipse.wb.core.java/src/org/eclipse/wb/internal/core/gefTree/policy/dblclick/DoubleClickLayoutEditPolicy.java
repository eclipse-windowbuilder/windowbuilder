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
package org.eclipse.wb.internal.core.gefTree.policy.dblclick;

import org.eclipse.wb.core.gefTree.part.JavaEditPart;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.policy.OpenListenerEditPolicy;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;

import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;

/**
 * An abstract descendant of {@link LayoutEditPolicy} for double-click handling in widgets tree.
 *
 * @author mitin_aa
 * @coverage core.gefTree.policy
 */
public abstract class DoubleClickLayoutEditPolicy extends EditPolicy
implements
IPreferenceConstants {
	protected final JavaInfo m_javaInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DoubleClickLayoutEditPolicy(JavaInfo javaInfo) {
		m_javaInfo = javaInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Installing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Installs {@link EditPolicy} for handling double click request, based on settings.
	 */
	public static void install(JavaEditPart editPart) {
		int mode = DesignerPlugin.getPreferences().getInt(P_EDITOR_TREE_DBL_CLICK_ACTION);
		JavaInfo javaInfo = editPart.getJavaInfo();
		// prepare policy
		EditPolicy editPolicy = null;
		switch (mode) {
		case V_EDITOR_TREE_OPEN_WIDGET_IN_EDITOR :
			editPolicy = new OpenEditorLayoutEditPolicy(javaInfo);
			break;
		case V_EDITOR_TREE_CREATE_LISTENER :
			editPolicy = new OpenListenerEditPolicy(javaInfo);
			break;
		case V_EDITOR_TREE_INITIATE_RENAME :
			editPolicy = new RenameJavaInfoLayoutEditPolicy(javaInfo);
			break;
		}
		// install policy
		editPart.installEditPolicy(DoubleClickLayoutEditPolicy.class, editPolicy);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Double-click
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void performRequest(Request request) {
		if (RequestConstants.REQ_OPEN.equals(request.getType())) {
			performDoubleClick();
		}
		super.performRequest(request);
	}

	/**
	 * Override this to do operations required when user double-clicks on widget in widgets tree.
	 */
	protected abstract void performDoubleClick();
}
