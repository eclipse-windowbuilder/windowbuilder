/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPageLayout;

/**
 * Element for {@link IPageLayout#addFastView(String)} method.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
@Deprecated(since = "1.9.1400", forRemoval = true)
public final class FastViewInfo extends AbstractShortcutInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	@Deprecated
	@SuppressWarnings("removal")
	public FastViewInfo(PageLayoutInfo page,
			FastViewContainerInfo container,
			MethodInvocation invocation) throws Exception {
		super(page, container, invocation);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rendering
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Deprecated
	protected ImageDescriptor getPresentationIcon() {
		return getViewInfo().getIcon();
	}

	@Override
	@Deprecated
	protected String getPresentationText() {
		return "\"" + getViewInfo().getName() + "\" - " + getId();
	}

	/**
	 * @return the {@link ViewInfo} for this view.
	 */
	private ViewInfo getViewInfo() {
		return PdeUtils.getViewInfoDefault(getId());
	}
}
