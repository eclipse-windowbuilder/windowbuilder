/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.gef.part;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.core.gef.policy.TabOrderContainerEditPolicy;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.gef.Request;

/**
 * {@link EditPart} for {@link ControlInfo}.
 *
 * @author lobas_av
 * @coverage swt.gef.part
 */
public class ControlEditPart extends AbstractComponentEditPart {

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ControlEditPart(ControlInfo control) {
		super(control);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Edit Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public EditPart getTargetEditPart(Request request) {
		if (TabOrderContainerEditPolicy.TAB_ORDER_REQUEST == request) {
			return this;
		}
		return super.getTargetEditPart(request);
	}
}