/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.internal.core.editor.DesignPage;
import org.eclipse.wb.internal.core.editor.Messages;

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
		setToolTipText(Messages.RefreshAction_toolTip);
		setImageDescriptor(CoreImages.EDITOR_REFRESH);
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