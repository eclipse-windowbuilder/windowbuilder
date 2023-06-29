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
package org.eclipse.wb.internal.rcp.gef.part.rcp;

import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.core.gef.EditPartFactory;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.ViewPartLikeInfo;

/**
 * {@link EditPart} for {@link ViewPartLikeInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class ViewPartLikeEditPart extends WorkbenchPartLikeEditPart {
	private final ViewPartLikeInfo m_part;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewPartLikeEditPart(ViewPartLikeInfo part) {
		super(part);
		m_part = part;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected EditPart createEditPart(Object model) {
		if (model instanceof MenuManagerInfo) {
			MenuManagerInfo manager = (MenuManagerInfo) model;
			EditPart editPart = MenuEditPartFactory.createPopupMenu(model, m_part.getMenuImpl(manager));
			EditPartFactory.configureEditPart(this, editPart);
			return editPart;
		}
		return super.createEditPart(model);
	}
}
