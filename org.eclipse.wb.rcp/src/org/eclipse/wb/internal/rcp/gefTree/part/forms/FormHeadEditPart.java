/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.rcp.gefTree.part.forms;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.gefTree.policy.forms.FormHeadLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.forms.FormInfo;
import org.eclipse.wb.internal.swt.gefTree.part.CompositeEditPart;

/**
 * {@link EditPart} for {@link FormInfo#getHead()}.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.part
 */
public final class FormHeadEditPart extends CompositeEditPart {
	private final FormInfo m_form;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormHeadEditPart(FormInfo item) {
		super(item.getHead());
		m_form = item;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		installEditPolicy(new FormHeadLayoutEditPolicy(m_form));
	}
}
