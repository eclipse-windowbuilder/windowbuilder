/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.xml.editor.actions;

import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;

/**
 * This action does switching between "Source" and "Design" tabs of {@link AbstractXmlEditor}.
 *
 * @author scheglov_ke
 * @coverage XML.editor.action
 */
public class SwitchAction extends EditorRelatedAction {
	////////////////////////////////////////////////////////////////////////////
	//
	// IActionDelegate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void run() {
		AbstractXmlEditor editor = getEditor();
		if (editor != null) {
			editor.switchSourceDesign();
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
		AbstractXmlEditor editor = getActiveEditor();
		if (editor != null) {
			editor.showSource();
			if (position != -1) {
				editor.showSourcePosition(position);
			}
		}
	}
}