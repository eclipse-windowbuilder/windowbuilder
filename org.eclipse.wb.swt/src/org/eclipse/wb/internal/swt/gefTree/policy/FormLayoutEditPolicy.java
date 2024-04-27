/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.gefTree.policy;

import org.eclipse.wb.core.gefTree.policy.ObjectLayoutEditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.IFormLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link FormLayoutInfo}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage swt.gefTree.policy
 */
public final class FormLayoutEditPolicy<C extends IControlInfo> extends ObjectLayoutEditPolicy<C> {
	private final IFormLayoutInfo<C> m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormLayoutEditPolicy(IFormLayoutInfo<C> layout) {
		super(layout.getUnderlyingModel());
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return ControlsLayoutRequestValidator.INSTANCE;
	}

	@Override
	protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
		Object model = editPart.getModel();
		return model instanceof IControlInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void command_CREATE(C control, C reference) throws Exception {
		m_layout.commandCreate(control, reference);
	}

	@Override
	protected void command_MOVE(C control, C reference) throws Exception {
		m_layout.commandMove(control, reference);
	}
}