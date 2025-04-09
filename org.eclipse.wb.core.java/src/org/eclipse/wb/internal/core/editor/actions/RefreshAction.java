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
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.DesignPage;

import org.eclipse.ui.actions.ActionFactory;

/**
 * Action for reparse/refresh {@link DesignPage}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action
 */
public final class RefreshAction extends DesignPageAction {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RefreshAction() {
		// copy presentation
		ActionUtils.copyPresentation(this, ActionFactory.REFRESH);
		// override presentation
		setToolTipText("Reparse the source and refresh the design page");
		setImageDescriptor(DesignerPlugin.getImageDescriptor("editor_refresh.png"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DesignPageAction
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void run(IDesignPage designPage) {
		designPage.refreshGEF();
	}
}