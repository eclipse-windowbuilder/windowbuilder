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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.actions;

import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.model.MigDimensionInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link Action} for modifying grow of {@link MigDimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
public final class SetGrowAction<T extends MigDimensionInfo> extends DimensionHeaderAction<T> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetGrowAction(DimensionHeaderEditPart<T> header,
			String text,
			ImageDescriptor imageDescriptor) {
		super(header, text, imageDescriptor, AS_CHECK_BOX);
		setChecked(header.getDimension().hasGrow());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void run(T dimension, int index) throws Exception {
		dimension.flipGrow();
	}
}