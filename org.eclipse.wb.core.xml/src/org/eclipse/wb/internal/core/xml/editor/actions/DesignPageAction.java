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
import org.eclipse.wb.internal.core.xml.editor.IXmlEditorPage;
import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;

/**
 * Action for {@link XmlDesignPage}.
 *
 * @author scheglov_ke
 * @coverage XML.editor.action
 */
public abstract class DesignPageAction extends EditorRelatedAction {
	////////////////////////////////////////////////////////////////////////////
	//
	// IAction
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void run() {
		AbstractXmlEditor editor = getEditor();
		if (editor != null) {
			IXmlEditorPage designPage = editor.getDesignPage();
			if (designPage instanceof XmlDesignPage) {
				run((XmlDesignPage) designPage);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DesignPageAction
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Runs the action with given {@link XmlDesignPage}.
	 */
	protected abstract void run(XmlDesignPage designPage);
}
