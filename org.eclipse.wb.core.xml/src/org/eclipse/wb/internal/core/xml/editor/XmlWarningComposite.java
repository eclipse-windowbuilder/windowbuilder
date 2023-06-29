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
package org.eclipse.wb.internal.core.xml.editor;

import org.eclipse.wb.internal.core.editor.errors.WarningComposite;
import org.eclipse.wb.internal.core.xml.editor.actions.RefreshAction;
import org.eclipse.wb.internal.core.xml.editor.actions.SwitchAction;

import org.eclipse.swt.widgets.Composite;

/**
 * Implementation for XML.
 *
 * @author scheglov_ke
 * @coverage XML.editor
 */
public final class XmlWarningComposite extends WarningComposite {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XmlWarningComposite(Composite parent, int style) {
		super(parent, style);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operations
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void doRefresh() {
		new RefreshAction().run();
	}

	@Override
	protected void doShowSource(int sourcePosition) {
		SwitchAction.showSource(sourcePosition);
	}
}
