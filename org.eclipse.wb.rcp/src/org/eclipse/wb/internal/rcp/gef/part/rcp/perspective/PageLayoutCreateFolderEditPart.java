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
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.PageLayoutCreateFolderLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.PageLayoutSidesLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutCreateFolderInfo;

/**
 * {@link EditPart} for {@link PageLayoutCreateFolderInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class PageLayoutCreateFolderEditPart extends AbstractComponentEditPart {
	private final PageLayoutCreateFolderInfo m_folder;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PageLayoutCreateFolderEditPart(PageLayoutCreateFolderInfo folder) {
		super(folder);
		m_folder = folder;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(new PageLayoutSidesLayoutEditPolicy(m_folder.getPage(), m_folder, false));
		installEditPolicy(new PageLayoutCreateFolderLayoutEditPolicy(m_folder));
	}

	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new AbstractPartSelectionEditPolicy(m_folder));
	}
}
