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
package org.eclipse.wb.internal.rcp.gef.part.rcp.perspective;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.AbstractPartSelectionEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.PageLayoutSidesLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutAddViewInfo;

/**
 * {@link EditPart} for {@link PageLayoutAddViewInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class PageLayoutAddViewEditPart extends AbstractComponentEditPart {
	private final PageLayoutAddViewInfo m_view;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PageLayoutAddViewEditPart(PageLayoutAddViewInfo view) {
		super(view);
		m_view = view;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(new PageLayoutSidesLayoutEditPolicy(m_view.getPage(), m_view, true));
	}

	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		installEditPolicy(EditPolicy.SELECTION_ROLE, new AbstractPartSelectionEditPolicy(m_view));
	}
}
