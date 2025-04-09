/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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