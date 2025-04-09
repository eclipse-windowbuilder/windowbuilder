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
package org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.selection;

import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.edit.RowHeaderEditPart;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link RowHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class RowSelectionEditPolicy<C extends IControlInfo>
extends
DimensionSelectionEditPolicy<C> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RowSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
		super(mainPolicy);
	}
}
