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
package org.eclipse.wb.internal.core.editor.actions;

import org.eclipse.wb.core.editor.IDesignPage;
import org.eclipse.wb.internal.core.editor.DesignPage;
import org.eclipse.wb.internal.core.editor.multi.DesignerEditor;

/**
 * Action for {@link DesignPage}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action
 */
public abstract class DesignPageAction extends EditorRelatedAction {
	////////////////////////////////////////////////////////////////////////////
	//
	// IAction
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void run() {
		DesignerEditor editor = getEditor();
		if (editor != null) {
			IDesignPage designPage = editor.getMultiMode().getDesignPage();
			run(designPage);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DesignPageAction
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Runs the action with given {@link DesignPage}.
	 */
	protected abstract void run(IDesignPage designPage);
}
