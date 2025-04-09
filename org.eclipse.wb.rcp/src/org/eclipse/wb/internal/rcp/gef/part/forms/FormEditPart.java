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
package org.eclipse.wb.internal.rcp.gef.part.forms;

import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.internal.core.gef.EditPartFactory;
import org.eclipse.wb.internal.rcp.model.forms.FormInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.swt.gef.part.CompositeEditPart;

import org.eclipse.gef.EditPart;

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
	protected EditPart createChild(Object model) {
		if (model instanceof MenuManagerInfo manager) {
			org.eclipse.wb.gef.core.EditPart editPart = MenuEditPartFactory.createPopupMenu(model, m_form.getMenuImpl(manager));
			EditPartFactory.configureEditPart(this, editPart);
			return editPart;
		}
		return super.createChild(model);
	}
}
