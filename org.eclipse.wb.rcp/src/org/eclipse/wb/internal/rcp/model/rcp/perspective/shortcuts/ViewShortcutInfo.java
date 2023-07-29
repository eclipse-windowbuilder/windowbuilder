/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts;

import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPageLayout;

/**
 * Element for {@link IPageLayout#addShowViewShortcut(String)} method.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class ViewShortcutInfo extends AbstractShortcutInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewShortcutInfo(PageLayoutInfo page,
			ViewShortcutContainerInfo container,
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
		return new ImageImageDescriptor(getViewInfo().getIcon());
	}

	@Override
	protected String getPresentationText() throws Exception {
		return "\"" + getViewInfo().getName() + "\" - " + getId();
	}

	/**
	 * @return the {@link ViewInfo} for this view.
	 */
	private ViewInfo getViewInfo() {
		return PdeUtils.getViewInfoDefault(getId());
	}
}
