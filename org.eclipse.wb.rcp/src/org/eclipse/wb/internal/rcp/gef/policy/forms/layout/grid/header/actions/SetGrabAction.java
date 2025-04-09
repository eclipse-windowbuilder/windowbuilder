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
package org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.actions;

import org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapDimensionInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link Action} for modifying grab of {@link TableWrapDimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class SetGrabAction<C extends IControlInfo> extends DimensionHeaderAction<C> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetGrabAction(DimensionHeaderEditPart<C> header,
			String text,
			ImageDescriptor imageDescriptor) {
		super(header, text, imageDescriptor, AS_CHECK_BOX);
		setChecked(header.getDimension().getGrab());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void run(TableWrapDimensionInfo<C> dimension) throws Exception {
		dimension.setGrab(!dimension.getGrab());
	}
}