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

import org.eclipse.wb.internal.core.editor.multi.DesignerEditor;

/**
 * This action does switching between "Source" and "Design" tabs of {@link DesignerEditor}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action
 */
public class SwitchAction extends EditorRelatedAction {
	////////////////////////////////////////////////////////////////////////////
	//
	// IActionDelegate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void run() {
		DesignerEditor editor = getEditor();
		if (editor != null) {
			editor.getMultiMode().switchSourceDesign();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Shows "Source" page.
	 */
	public static void showSource() {
		showSource(-1);
	}

	/**
	 * Shows "Source" page and at given source position.
	 *
	 * @param position
	 *          the position to show in source, if <code>-1</code>, then ignored.
	 */
	public static void showSource(int position) {
		DesignerEditor editor = getActiveEditor();
		if (editor != null) {
			editor.getMultiMode().showSource();
			if (position != -1) {
				editor.showSourcePosition(position);
			}
		}
	}
}