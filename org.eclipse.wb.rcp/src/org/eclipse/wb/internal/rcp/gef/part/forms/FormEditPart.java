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
package org.eclipse.wb.internal.rcp.gef.part.forms;

import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.core.gef.EditPartFactory;
import org.eclipse.wb.internal.rcp.model.forms.FormInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.swt.gef.part.CompositeEditPart;

/**
 * {@link EditPart} for {@link FormInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class FormEditPart extends CompositeEditPart {
	private final FormInfo m_form;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormEditPart(FormInfo form) {
		super(form);
		m_form = form;
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
			EditPart editPart = MenuEditPartFactory.createPopupMenu(model, m_form.getMenuImpl(manager));
			EditPartFactory.configureEditPart(this, editPart);
			return editPart;
		}
		return super.createEditPart(model);
	}
}
