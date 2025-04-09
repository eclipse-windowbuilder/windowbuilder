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
package org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts;

import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.PerspectiveInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPageLayout;

/**
 * Element for {@link IPageLayout#addPerspectiveShortcut(String)} method.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class PerspectiveShortcutInfo extends AbstractShortcutInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PerspectiveShortcutInfo(PageLayoutInfo page,
			PerspectiveShortcutContainerInfo container,
			MethodInvocation invocation) throws Exception {
		super(page, container, invocation);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rendering
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ImageDescriptor getPresentationIcon() throws Exception {
		return getPerspectiveInfo().getIcon();
	}

	@Override
	protected String getPresentationText() throws Exception {
		return "\"" + getPerspectiveInfo().getName() + "\" - " + getId();
	}

	/**
	 * @return the {@link PerspectiveInfo} for this perspective.
	 */
	private PerspectiveInfo getPerspectiveInfo() {
		return PdeUtils.getPerspectiveInfoDefault(getId());
	}
}
