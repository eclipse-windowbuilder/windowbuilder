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
package org.eclipse.wb.internal.rcp.gef.part.rcp;

import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.internal.core.gef.EditPartFactory;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.ViewPartLikeInfo;

import org.eclipse.gef.EditPart;

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
	protected EditPart createChild(Object model) {
		if (model instanceof MenuManagerInfo manager) {
			org.eclipse.wb.gef.core.EditPart editPart = MenuEditPartFactory.createPopupMenu(model, m_part.getMenuImpl(manager));
			EditPartFactory.configureEditPart(this, editPart);
			return editPart;
		}
		return super.createChild(model);
	}
}
