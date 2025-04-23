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
package org.eclipse.wb.internal.core.editor;

import org.eclipse.wb.core.controls.flyout.IFlyoutMenuContributor;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;

/**
 * {@link IFlyoutMenuContributor} for structure and palette.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public final class DesignerFlyoutMenuContributor implements IFlyoutMenuContributor {
	private final String m_viewId;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DesignerFlyoutMenuContributor(String viewId) {
		m_viewId = viewId;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IFlyoutMenuContributor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void contribute(IMenuManager manager) {
		manager.add(new Action("Extract as view") {
			@Override
			public void run() {
				ExecutionUtils.runLog(() -> DesignerPlugin.getActivePage().showView(m_viewId));
			}
		});
	}
}